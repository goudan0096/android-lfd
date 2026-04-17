# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean and rebuild
./gradlew clean assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run a specific test class
./gradlew test --tests "com.warehouse.manager.*"

# Generate KSP/Room sources (runs automatically with build)
./gradlew kspDebugKotlin
```

## Project Overview

A warehouse management Android app for managing product codes/locations with GPS-based navigation. The app supports:
- Product CRUD operations with GPS coordinates
- Stock in/out record tracking
- Barcode scanning for product entry
- GPS-based navigation with distance-based visual feedback (blinking rate)

## Architecture

**Pattern**: MVVM with Android Architecture Components

**Layers**:
- `data/model/` — Room entities (Product, StockRecord) and enums (ProductStatus, StockAction)
- `data/dao/` — Room DAOs with suspend functions and LiveData queries
- `data/repository/` — Repository classes wrapping DAOs
- `data/database/` — AppDatabase (Room) and TypeConverters
- `data/DataManager.kt` — JSON export/import via Gson
- `ui/viewmodel/ProductViewModel.kt` — Central ViewModel for all features, manages database + stock records
- `ui/adapter/` — RecyclerView adapters
- `view/` — Activities (MainActivity, AddEditProductActivity, NavigationActivity, HistoryActivity, BatchInActivity, SettingsActivity)
- `util/LocationHelper.kt` — GPS location utilities

**Database Schema** (Room, v3):
- `products` — id, code, name, location, latitude, longitude, status, createdAt, updatedAt
- `stock_records` — id, productId, productCode, productName, location, action, timestamp

**No DI framework** — ViewModel creates its own database/repository instances in `init {}`. Database uses singleton pattern.

## Key Dependencies

- Room 2.6.1 with KSP annotation processing
- Lifecycle/ViewModel 2.7.0
- Coroutines 1.7.3
- Google Play Services Location 21.1.0
- CameraX 1.3.1 + ML Kit Barcode Scanning 17.3.0
- Material 1.11.0

## Navigation Blinking Logic

`ProductViewModel.getBlinkInterval(distance)` returns interval in ms based on distance thresholds:
- ≤5m → 100ms, ≤10m → 200ms, ≤20m → 350ms, ≤50m → 500ms, ≤100m → 800ms, ≤200m → 1200ms, >200m → 2000ms

## Common Patterns

- ViewModel is shared across multiple Activities via `ViewModelProvider`
- Stock records are created automatically when products are inserted/updated with a `StockAction`
- Batch operations update product status and create records for each selected product
- Search uses SQL LIKE queries across code, name, and location fields
