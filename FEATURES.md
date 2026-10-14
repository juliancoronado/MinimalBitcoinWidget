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

---

### 2. 30-Day Historical Trendline Data Integration
* **Concept:** Fetch and render real 30-day Bitcoin price trendline data on the companion app's Dashboard, replacing the current "Chart unavailable for 30D interval" placeholder overlay.
* **Architecture & CoinGecko Quota Safeguards:**
  - **The Problem:** CoinGecko's `/coins/markets` endpoint only supplies 7-day sparklines (`sparkline_in_7d`). Obtaining 30-day historical data requires calling `/coins/bitcoin/market_chart?vs_currency={currency}&days=30`. Querying both endpoints on every 30-minute refresh across 8 currencies would consume ~23,000 calls/month, exceeding the free Demo API tier (10,000 monthly credits).
  - **The Solution (Asymmetric Caching):**
    - The backend (`mbw-backend`) bundles 30D data into the single `/api/bitcoinPrice` endpoint so the mobile app only ever makes 1 HTTP request.
    - **Dual TTL Caching in Firestore**:
      1. Spot price & 7D data: 30-minute cache TTL.
      2. 30D historical chart: **4-hour cache TTL** (capped at at most 6 calls / day per active currency).
    - **Live Tip Stitching**: When serving a cached 30D curve alongside a fresh 30-minute spot price, the backend updates the very last point to match `current_price`.
    - **Downsampling**: Raw hourly data from CoinGecko (~720 points) is downsampled to **4-hour intervals (~180 points)**, strictly matching the visual and tactile feel of the 7-day sparkline (168 points).
    - **Graceful Fallback**: If CoinGecko's `market_chart` times out or errors, the backend serves the spot price + 7D sparkline (retaining the previous 30D curve if available) so core price updates are never blocked.

* **Implementation Specification for AI Agents:**
  1. **Backend (`mbw-backend/functions/src/index.ts`)**:
     - Query `/coins/markets` and `/coins/bitcoin/market_chart?days=30` in parallel using `Promise.allSettled`.
     - Implement `downsample30dPrices()` to extract every 4th point and preserve the latest price.
     - Store `sparkline_in_30d: { price: number[] }` on the response object.
     - Deploy function and verify CDN caching headers.
  2. **Data Model (`classes/PriceData.kt`)**:
     - Add `@SerializedName("sparkline_in_30d") val sparklineIn30d: SparklineData? = null`.
     - In `getSparklineForInterval(index: Int)`, route index `2` to `sparklineIn30d?.prices ?: emptyList()`.
     - Preserve existing endpoint trend normalization math against `priceChangePercentage30d`.
  3. **Touch Scrubbing (`ui/components/SparklineGraph.kt`)**:
     - Add `stepDurationMillis: Long = 3600_000L` parameter.
     - Calculate `timestampAtPoint = lastUpdatedTimestamp - (pointsAgo * stepDurationMillis)`.
     - Pass `stepDurationMillis` into `pointerInput` keys.
  4. **Main Screen UI (`screens/MainScreen.kt`)**:
     - Update availability check: `val isChartUnavailable = uiState.sparklinePrices.isEmpty()`.
     - Pass `stepDurationMillis = if (is30dInterval) 4 * 3600_000L else 3600_000L`.
  5. **Mock Data & Testing (`PriceRepository.kt` & `PriceDataTest.kt`)**:
     - Generate 180 synthetic mock points for `sparklineIn30d` in `getMockPriceData()`.
     - Add unit tests verifying index 2 returns 0 points when null and full points when populated.

