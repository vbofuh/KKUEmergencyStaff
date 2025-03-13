// พาธ: com.kku.emergencystaff/main/incidents/repository/IncidentsRepository.kt
package com.example.sosstaff.main.incidents.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.sosstaff.models.Incident
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

class IncidentsRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val incidentsCollection = "incidents"
    private val chatCollection = "chats"

    // ดึงรายการเหตุการณ์ทั้งหมดที่ยังไม่เสร็จสิ้น
    fun getActiveIncidents(): LiveData<List<Incident>> {
        val incidentsLiveData = MutableLiveData<List<Incident>>()

        firestore.collection(incidentsCollection)
            .whereNotEqualTo("status", "เสร็จสิ้น")
            .orderBy("status")
            .orderBy("reportedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val incidents = snapshot.toObjects(Incident::class.java)
                    incidentsLiveData.value = incidents
                }
            }

        return incidentsLiveData
    }

    // ดึงรายการเหตุการณ์ที่เสร็จสิ้นแล้ว
    fun getCompletedIncidents(): LiveData<List<Incident>> {
        val incidentsLiveData = MutableLiveData<List<Incident>>()

        firestore.collection(incidentsCollection)
            .whereEqualTo("status", "เสร็จสิ้น")
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val incidents = snapshot.toObjects(Incident::class.java)
                    incidentsLiveData.value = incidents
                }
            }

        return incidentsLiveData
    }

    // ดึงรายละเอียดของเหตุการณ์ตาม ID
    fun getIncidentById(incidentId: String): LiveData<Incident?> {
        val incidentLiveData = MutableLiveData<Incident?>()

        firestore.collection(incidentsCollection)
            .document(incidentId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val incident = snapshot.toObject(Incident::class.java)
                    incidentLiveData.value = incident
                } else {
                    incidentLiveData.value = null
                }
            }

        return incidentLiveData
    }

    // อัปเดตสถานะของเหตุการณ์
    fun updateIncidentStatus(incidentId: String, newStatus: String): LiveData<Boolean> {
        val resultLiveData = MutableLiveData<Boolean>()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            resultLiveData.value = false
            return resultLiveData
        }

        val updates = hashMapOf<String, Any>(
            "status" to newStatus,
            "lastUpdatedAt" to Date()
        )

        // ถ้าเปลี่ยนเป็นเสร็จสิ้น ให้เพิ่มเวลาเสร็จสิ้น
        if (newStatus == "เสร็จสิ้น") {
            updates["completedAt"] = Date()

            // อัปเดตสถานะห้องแชทให้ไม่ใช้งาน
            firestore.collection(chatCollection)
                .document(incidentId)
                .update("active", false)
        }

        // ถ้าเปลี่ยนเป็นเจ้าหน้าที่รับเรื่องแล้ว และยังไม่มีเจ้าหน้าที่รับผิดชอบ ให้กำหนดเจ้าหน้าที่
        if (newStatus == "เจ้าหน้าที่รับเรื่องแล้ว") {
            // ตรวจสอบเจ้าหน้าที่ปัจจุบัน
            firestore.collection(incidentsCollection)
                .document(incidentId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val incident = document.toObject(Incident::class.java)

                        if (incident != null && incident.assignedStaffId.isEmpty()) {
                            // ดึงข้อมูลเจ้าหน้าที่ปัจจุบัน
                            firestore.collection("staff")
                                .document(currentUser.uid)
                                .get()
                                .addOnSuccessListener { staffDoc ->
                                    if (staffDoc.exists()) {
                                        val staffName = staffDoc.getString("name") ?: ""

                                        updates["assignedStaffId"] = currentUser.uid
                                        updates["assignedStaffName"] = staffName

                                        // อัปเดตข้อมูลในห้องแชทด้วย
                                        firestore.collection(chatCollection)
                                            .document(incidentId)
                                            .update(
                                                mapOf(
                                                    "staffId" to currentUser.uid,
                                                    "staffName" to staffName
                                                )
                                            )
                                    }

                                    // ทำการอัปเดตสถานะ
                                    updateIncidentDoc(incidentId, updates, resultLiveData)
                                }
                        } else {
                            // ทำการอัปเดตสถานะ
                            updateIncidentDoc(incidentId, updates, resultLiveData)
                        }
                    }
                }
        } else {
            // ทำการอัปเดตสถานะ
            updateIncidentDoc(incidentId, updates, resultLiveData)
        }

        return resultLiveData
    }

    private fun updateIncidentDoc(incidentId: String, updates: Map<String, Any>, resultLiveData: MutableLiveData<Boolean>) {
        firestore.collection(incidentsCollection)
            .document(incidentId)
            .update(updates)
            .addOnSuccessListener {
                resultLiveData.value = true
            }
            .addOnFailureListener {
                resultLiveData.value = false
            }
    }

    // ค้นหาเหตุการณ์
    fun searchIncidents(query: String): LiveData<List<Incident>> {
        val incidentsLiveData = MutableLiveData<List<Incident>>()

        // เนื่องจาก Firestore ไม่รองรับการค้นหาข้อความแบบ full-text ตรงๆ
        // เราจะดึงข้อมูลทั้งหมดมาก่อนแล้วกรองในแอป
        firestore.collection(incidentsCollection)
            .orderBy("reportedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val allIncidents = documents.toObjects(Incident::class.java)

                // กรองเหตุการณ์ตามข้อความค้นหา
                val filteredIncidents = allIncidents.filter { incident ->
                    incident.incidentType.contains(query, ignoreCase = true) ||
                            incident.location.contains(query, ignoreCase = true) ||
                            incident.reporterName.contains(query, ignoreCase = true) ||
                            incident.additionalInfo.contains(query, ignoreCase = true)
                }

                incidentsLiveData.value = filteredIncidents
            }
            .addOnFailureListener {
                incidentsLiveData.value = emptyList()
            }

        return incidentsLiveData
    }
}