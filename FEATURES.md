# Minimal Bitcoin Widget - Feature Backlog & Ideas

This document outlines feature ideas, roadmap proposals, and possible future features for **Minimal Bitcoin Widget**.

---

## Proposed Features

### 1. Daily "Price Change Summary" Smart Notification
* **Concept:** An optional, unobtrusive morning/evening digest notification delivering a high-level summary of Bitcoin market movements over the past 24 hours.
* **Example Content:**
  > *"☕ Morning BTC Brief: $96,400 (+3.2% in 24h). Daily high: $97,100."*
* **Key Capabilities:**
  - **Configurable Delivery Time:** User selects morning (e.g., 8:00 AM) and/or evening (e.g., 8:00 PM) delivery.
  - **Zero Server Footprint:** Schedules local notifications utilizing Android `WorkManager` with battery-efficient constraints.
  - **Customizable Metrics:** Toggle between 24h % change, daily high/low range, and selected local fiat currency.
  - **Notification Actions:** Quick action button to directly refresh widgets or open the Dashboard.
