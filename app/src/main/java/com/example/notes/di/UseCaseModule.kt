
package com.example.notes.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Use case module for dependency injection.
 *
 * Note: Use cases are now auto-provided by Hilt via @Inject constructor.
 * This module can be used for additional use case configurations if needed.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
  // Use cases are auto-provided by Hilt via @Inject constructor
  // No manual @Provides methods needed
}

