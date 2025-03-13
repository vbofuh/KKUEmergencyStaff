// com.kku.emergencystaff/main/incidents/viewmodels/IncidentsViewModel.kt
package com.example.sosstaff.main.incidents.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sosstaff.main.incidents.repository.IncidentsRepository
import com.example.sosstaff.models.Incident

class IncidentsViewModel(
    private val incidentsRepository: IncidentsRepository
) : ViewModel() {

    // LiveData for active incidents
    val activeIncidents: LiveData<List<Incident>> = incidentsRepository.getActiveIncidents()

    // LiveData for completed incidents
    val completedIncidents: LiveData<List<Incident>> = incidentsRepository.getCompletedIncidents()

    // LiveData for search results
    private val _searchResults = MutableLiveData<List<Incident>>()
    val searchResults: LiveData<List<Incident>> get() = _searchResults

    // LiveData for incident type filter
    private val _filteredByType = MutableLiveData<String?>(null)

    // LiveData that combines filtering and searching
    private val _filteredActiveIncidents = MediatorLiveData<List<Incident>>()
    val filteredActiveIncidents: LiveData<List<Incident>> get() = _filteredActiveIncidents

    private val _filteredCompletedIncidents = MediatorLiveData<List<Incident>>()
    val filteredCompletedIncidents: LiveData<List<Incident>> get() = _filteredCompletedIncidents

    init {
        setupFilteredIncidents()
    }

    private fun setupFilteredIncidents() {
        // Track changes in active incidents
        _filteredActiveIncidents.addSource(activeIncidents) { incidents ->
            _filteredActiveIncidents.value = applyFilters(incidents)
        }

        // Track changes in filter
        _filteredActiveIncidents.addSource(_filteredByType) { type ->
            val currentIncidents = activeIncidents.value ?: emptyList()
            _filteredActiveIncidents.value = applyFilters(currentIncidents)
        }

        // Track changes in completed incidents
        _filteredCompletedIncidents.addSource(completedIncidents) { incidents ->
            _filteredCompletedIncidents.value = applyFilters(incidents)
        }

        // Track changes in filter for completed incidents
        _filteredCompletedIncidents.addSource(_filteredByType) { type ->
            val currentIncidents = completedIncidents.value ?: emptyList()
            _filteredCompletedIncidents.value = applyFilters(currentIncidents)
        }
    }

    // Filter incidents by type
    private fun applyFilters(incidents: List<Incident>): List<Incident> {
        val incidentType = _filteredByType.value

        return if (incidentType.isNullOrEmpty()) {
            incidents
        } else {
            incidents.filter { it.incidentType == incidentType }
        }
    }

    // Set incident type filter
    fun filterByType(incidentType: String?) {
        _filteredByType.value = incidentType
    }

    // Search incidents
    fun searchIncidents(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        incidentsRepository.searchIncidents(query).observeForever { incidents ->
            _searchResults.value = incidents
        }
    }

    // Update incident status
    fun updateIncidentStatus(incidentId: String, newStatus: String): LiveData<Boolean> {
        return incidentsRepository.updateIncidentStatus(incidentId, newStatus)
    }

    // Get incident by ID
    fun getIncidentById(incidentId: String): LiveData<Incident?> {
        return incidentsRepository.getIncidentById(incidentId)
    }

    // Load unassigned incidents
    fun loadUnassignedIncidents(): LiveData<List<Incident>> {
        return incidentsRepository.getUnassignedIncidents()
    }
}