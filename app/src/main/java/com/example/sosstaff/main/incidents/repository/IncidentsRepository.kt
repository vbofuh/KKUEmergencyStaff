// app/src/main/java/com/example/sosstaff/main/incidents/repository/IncidentsRepository.kt

package com.example.sosstaff.main.incidents.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.sosstaff.models.Incident
import java.util.Date

class IncidentsRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val incidentsCollection = "incidents"
    private val chatCollection = "chats"

    // Fix for getUnassignedIncidents - undefined db variable in original code
    fun getUnassignedIncidents(): LiveData<List<Incident>> {
        val incidentsLiveData = MutableLiveData<List<Incident>>()

        firestore.collection(incidentsCollection)
            .whereEqualTo("assignedStaffId", "")
            .orderBy("reportedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val incidents = snapshot.toObjects(Incident::class.java)
                    incidentsLiveData.value = incidents
                } else {
                    incidentsLiveData.value = emptyList()
                }
            }

        return incidentsLiveData
    }

    fun getActiveIncidents(): LiveData<List<Incident>> {
        val incidentsLiveData = MutableLiveData<List<Incident>>()

        firestore.collection(incidentsCollection)
            .whereNotEqualTo("status", "เสร็จสิ้น")
            .orderBy("status")
            .orderBy("reportedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    incidentsLiveData.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val incidents = snapshot.toObjects(Incident::class.java)
                    incidentsLiveData.value = incidents
                } else {
                    incidentsLiveData.value = emptyList()
                }
            }

        return incidentsLiveData
    }

    fun getCompletedIncidents(): LiveData<List<Incident>> {
        val incidentsLiveData = MutableLiveData<List<Incident>>()

        firestore.collection(incidentsCollection)
            .whereEqualTo("status", "เสร็จสิ้น")
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    incidentsLiveData.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val incidents = snapshot.toObjects(Incident::class.java)
                    incidentsLiveData.value = incidents
                } else {
                    incidentsLiveData.value = emptyList()
                }
            }

        return incidentsLiveData
    }

    fun getIncidentById(incidentId: String): LiveData<Incident?> {
        val incidentLiveData = MutableLiveData<Incident?>()

        firestore.collection(incidentsCollection)
            .document(incidentId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    incidentLiveData.value = null
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

    fun updateIncidentStatus(incidentId: String, newStatus: String): LiveData<Boolean> {
        val resultLiveData = MutableLiveData<Boolean>()
        val currentUser = auth.currentUser

        // Add timestamp to updates
        val updates = hashMapOf<String, Any>(
            "status" to newStatus,
            "lastUpdatedAt" to Date()
        )

        // If completing the incident, add completedAt
        if (newStatus == "เสร็จสิ้น") {
            updates["completedAt"] = Date()

            // Also update chat room status to inactive
            firestore.collection("chats")
                .document(incidentId)
                .update("active", false)
        }

        // If changing to "เจ้าหน้าที่รับเรื่องแล้ว" and no staff assigned, assign current staff
        if (newStatus == "เจ้าหน้าที่รับเรื่องแล้ว") {
            // Check current incident
            firestore.collection(incidentsCollection)
                .document(incidentId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val incident = document.toObject(Incident::class.java)

                        if (incident != null && incident.assignedStaffId.isEmpty()) {
                            // Get current staff info
                            firestore.collection("staff")
                                .document(currentUser.uid)
                                .get()
                                .addOnSuccessListener { staffDoc ->
                                    if (staffDoc.exists()) {
                                        val staffName = staffDoc.getString("name") ?: ""

                                        updates["assignedStaffId"] = currentUser.uid
                                        updates["assignedStaffName"] = staffName

                                        // Also update chat room
                                        firestore.collection(chatCollection)
                                            .document(incidentId)
                                            .update(
                                                mapOf(
                                                    "staffId" to currentUser.uid,
                                                    "staffName" to staffName
                                                )
                                            )
                                    }

                                    // Update status
                                    updateIncidentDoc(incidentId, updates, resultLiveData)
                                }
                        } else {
                            // Update status
                            updateIncidentDoc(incidentId, updates, resultLiveData)
                        }
                    }
                }
        } else {
            // Update status
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

    fun searchIncidents(query: String): LiveData<List<Incident>> {
        val incidentsLiveData = MutableLiveData<List<Incident>>()

        // Since Firestore doesn't support direct full-text search
        // we'll fetch all incidents and filter in the app
        firestore.collection(incidentsCollection)
            .orderBy("reportedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val allIncidents = documents.toObjects(Incident::class.java)

                // Filter incidents by search query
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