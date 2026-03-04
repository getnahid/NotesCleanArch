package com.example.notes.presenter

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.notes.databinding.ActivityNoteDetailBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoteDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteDetailBinding
    private val viewModel: NoteDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val body = binding.etBody.text.toString()
            viewModel.updateNote(title, body)
        }

        binding.btnDelete.setOnClickListener {
            viewModel.deleteNote()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

                        state.note?.let { note ->
                            if (binding.etTitle.text.isNullOrEmpty()) {
                                binding.etTitle.setText(note.title)
                            }
                            if (binding.etBody.text.isNullOrEmpty()) {
                                binding.etBody.setText(note.body)
                            }
                        }
                    }
                }

                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is NoteDetailUiEvent.ShowError -> {
                                Snackbar.make(
                                    binding.root,
                                    event.message,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                            is NoteDetailUiEvent.NavigateBack -> {
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

