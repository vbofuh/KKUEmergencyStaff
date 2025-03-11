// พาธ: com.kku.emergencystaff/main/incidents/viewmodels/IncidentsViewModel.kt
package com.example.sosstaff.main.incidents.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sosstaff.main.incidents.repository.IncidentsRepository
import com.example.sosstaff.models.Incident
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class IncidentsViewModel @Inject constructor(
    private val incidentsRepository: IncidentsRepository
) : ViewModel() {

    // LiveData สำหรับเหตุการณ์ที่กำลังดำเนินการ
    val activeIncidents: LiveData<List<Incident>> get() = incidentsRepository.getActiveIncidents()

    // LiveData สำหรับเหตุการณ์ที่เสร็จสิ้นแล้ว
    val completedIncidents: LiveData<List<Incident>> get() = incidentsRepository.getCompletedIncidents()

    // LiveData สำหรับผลการค้นหา
    private val _searchResults = MutableLiveData<List<Incident>>()
    val searchResults: LiveData<List<Incident>> get() = _searchResults

    // LiveData สำหรับการกรองประเภทเหตุการณ์
    private val _filteredByType = MutableLiveData<String?>(null)

    // LiveData ที่รวมผลการกรองและการค้นหา
    private val _filteredActiveIncidents = MediatorLiveData<List<Incident>>()
    val filteredActiveIncidents: LiveData<List<Incident>> get() = _filteredActiveIncidents

    private val _filteredCompletedIncidents = MediatorLiveData<List<Incident>>()
    val filteredCompletedIncidents: LiveData<List<Incident>> get() = _filteredCompletedIncidents

    init {
        setupFilteredIncidents()
    }

    private fun setupFilteredIncidents() {
        // ติดตามการเปลี่ยนแปลงของเหตุการณ์ที่กำลังดำเนินการ
        _filteredActiveIncidents.addSource(activeIncidents) { incidents ->
            _filteredActiveIncidents.value = applyFilters(incidents)
        }

        // ติดตามการเปลี่ยนแปลงของการกรอง
        _filteredActiveIncidents.addSource(_filteredByType) { type ->
            val currentIncidents = activeIncidents.value ?: emptyList()
            _filteredActiveIncidents.value = applyFilters(currentIncidents)
        }

        // ติดตามการเปลี่ยนแปลงของเหตุการณ์ที่เสร็จสิ้นแล้ว
        _filteredCompletedIncidents.addSource(completedIncidents) { incidents ->
            _filteredCompletedIncidents.value = applyFilters(incidents)
        }

        // ติดตามการเปลี่ยนแปลงของการกรอง
        _filteredCompletedIncidents.addSource(_filteredByType) { type ->
            val currentIncidents = completedIncidents.value ?: emptyList()
            _filteredCompletedIncidents.value = applyFilters(currentIncidents)
        }
    }

    // กรองเหตุการณ์ตามประเภท
    private fun applyFilters(incidents: List<Incident>): List<Incident> {
        val incidentType = _filteredByType.value

        return if (incidentType.isNullOrEmpty()) {
            incidents
        } else {
            incidents.filter { it.incidentType == incidentType }
        }
    }

    // กำหนดประเภทเหตุการณ์ที่ต้องการกรอง
    fun filterByType(incidentType: String?) {
        _filteredByType.value = incidentType
    }

    // ค้นหาเหตุการณ์
    fun searchIncidents(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        incidentsRepository.searchIncidents(query).observeForever { incidents ->
            _searchResults.value = incidents
        }
    }

    // อัปเดตสถานะของเหตุการณ์
    fun updateIncidentStatus(incidentId: String, newStatus: String): LiveData<Boolean> {
        return incidentsRepository.updateIncidentStatus(incidentId, newStatus)
    }

    // ดึงรายละเอียดของเหตุการณ์ตาม ID
    fun getIncidentById(incidentId: String): LiveData<Incident?> {
        return incidentsRepository.getIncidentById(incidentId)
    }
}