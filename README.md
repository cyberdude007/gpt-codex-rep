# PaisaSplit

Scaffolded Jetpack Compose app for tracking expenses and settlements. This early UI shell includes:

- Bottom navigation for Home, Parties, Stats and Settings with a center FAB to add items.
- High contrast light/dark themes and Indian currency/date defaults.
- Demo data loaded from `app/src/main/assets/data/seed/demo_seed.json` into an in-memory repository.
- Basic settings toggles including theme and "hide amounts".

## Build

```bash
gradle :app:lint :app:testDebugUnitTest
gradle :app:assembleDebug
```

Debug APKs are uploaded on each pull request via GitHub Actions.
