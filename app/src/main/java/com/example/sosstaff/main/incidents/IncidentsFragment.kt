// พาธ: com.kku.emergencystaff/main/incidents/IncidentsFragment.kt
package com.example.sosstaff.main.incidents

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.example.sosstaff.R
import com.example.sosstaff.databinding.FragmentIncidentsBinding
import com.example.sosstaff.main.incidents.adapters.IncidentAdapter
import com.example.sosstaff.main.incidents.viewmodels.IncidentsViewModel
import com.example.sosstaff.models.Incident
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IncidentsFragment : Fragment() {

    private var _binding: FragmentIncidentsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: IncidentsViewModel by viewModels()
    private lateinit var activeIncidentsAdapter: IncidentAdapter
    private lateinit var completedIncidentsAdapter: IncidentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIncidentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupTabs()
        setupSearch()
        setupFilterChips()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        // ตั้งค่า RecyclerView สำหรับเหตุการณ์ที่กำลังดำเนินการ
        activeIncidentsAdapter = IncidentAdapter { incident ->
            navigateToIncidentDetail(incident)
        }
        binding.rvActiveIncidents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = activeIncidentsAdapter
        }

        // ตั้งค่า RecyclerView สำหรับเหตุการณ์ที่เสร็จสิ้นแล้ว
        completedIncidentsAdapter = IncidentAdapter { incident ->
            navigateToIncidentDetail(incident)
        }
        binding.rvCompletedIncidents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = completedIncidentsAdapter
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showActiveIncidents()
                    1 -> showCompletedIncidents()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // เริ่มต้นที่แท็บเหตุการณ์ที่กำลังดำเนินการ
        binding.tabLayout.getTabAt(0)?.select()
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    viewModel.searchIncidents(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    // เมื่อล้างข้อความค้นหา ให้กลับไปแสดงรายการปกติ
                    observeViewModel()
                }
                return true
            }
        })
    }

    private fun setupFilterChips() {
        binding.chipAll.setOnClickListener { viewModel.filterByType(null) }
        binding.chipAccident.setOnClickListener { viewModel.filterByType("อุบัติเหตุบนถนน") }
        binding.chipAnimal.setOnClickListener { viewModel.filterByType("จับสัตว์") }
        binding.chipFight.setOnClickListener { viewModel.filterByType("ทะเลาะวิวาท") }
        binding.chipOther.setOnClickListener { viewModel.filterByType("อื่นๆ") }

        // เริ่มต้นเลือกตัวกรองทั้งหมด
        binding.chipGroup.check(R.id.chipAll)
    }

    private fun observeViewModel() {
        // สังเกตการณ์เหตุการณ์ที่กำลังดำเนินการ
        viewModel.filteredActiveIncidents.observe(viewLifecycleOwner) { incidents ->
            activeIncidentsAdapter.submitList(incidents)
            updateActiveIncidentsEmptyView(incidents.isEmpty())
        }

        // สังเกตการณ์เหตุการณ์ที่เสร็จสิ้นแล้ว
        viewModel.filteredCompletedIncidents.observe(viewLifecycleOwner) { incidents ->
            completedIncidentsAdapter.submitList(incidents)
            updateCompletedIncidentsEmptyView(incidents.isEmpty())
        }

        // สังเกตการณ์ผลการค้นหา
        viewModel.searchResults.observe(viewLifecycleOwner) { incidents ->
            // แบ่งผลการค้นหาเป็นเหตุการณ์ที่กำลังดำเนินการและเหตุการณ์ที่เสร็จสิ้นแล้ว
            val activeIncidents = incidents.filter { it.isActive() }
            val completedIncidents = incidents.filter { !it.isActive() }

            activeIncidentsAdapter.submitList(activeIncidents)
            completedIncidentsAdapter.submitList(completedIncidents)

            updateActiveIncidentsEmptyView(activeIncidents.isEmpty())
            updateCompletedIncidentsEmptyView(completedIncidents.isEmpty())
        }
    }

    private fun showActiveIncidents() {
        binding.rvActiveIncidents.visibility = View.VISIBLE
        binding.rvCompletedIncidents.visibility = View.GONE
        binding.tvEmptyActive.visibility = if (activeIncidentsAdapter.itemCount == 0) View.VISIBLE else View.GONE
        binding.tvEmptyCompleted.visibility = View.GONE
    }

    private fun showCompletedIncidents() {
        binding.rvActiveIncidents.visibility = View.GONE
        binding.rvCompletedIncidents.visibility = View.VISIBLE
        binding.tvEmptyActive.visibility = View.GONE
        binding.tvEmptyCompleted.visibility = if (completedIncidentsAdapter.itemCount == 0) View.VISIBLE else View.GONE
    }

    private fun updateActiveIncidentsEmptyView(isEmpty: Boolean) {
        binding.tvEmptyActive.visibility = if (isEmpty && binding.rvActiveIncidents.visibility == View.VISIBLE) View.VISIBLE else View.GONE
    }

    private fun updateCompletedIncidentsEmptyView(isEmpty: Boolean) {
        binding.tvEmptyCompleted.visibility = if (isEmpty && binding.rvCompletedIncidents.visibility == View.VISIBLE) View.VISIBLE else View.GONE
    }

    private fun navigateToIncidentDetail(incident: Incident) {
        val intent = Intent(requireContext(), IncidentDetailActivity::class.java).apply {
            putExtra(IncidentDetailActivity.EXTRA_INCIDENT_ID, incident.id)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}