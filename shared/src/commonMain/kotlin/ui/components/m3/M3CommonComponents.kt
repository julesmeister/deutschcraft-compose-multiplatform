// M3 Common Components - Complete UI Component Library
// Import individual components as needed:
//   import ui.components.m3.M3ValueBadge
//   import ui.components.m3.DCBackButton
//   import ui.components.m3.M3ListCard
//   etc.

package ui.components.m3

// =============================================================================
// BADGES & INDICATORS
// =============================================================================
// M3Badges.kt           - M3ValueBadge, M3ToggleBadge
// M3BadgesExtra.kt      - M3DateBadge, M3StatusBadge, M3ToggleBadgePill,
//                         TonalBadge, M3SummaryTab
// StatusDot.kt          - Simple colored status indicator dot
// NotificationBadge.kt  - Badge with count (99+)

// =============================================================================
// LAYOUT & LIST COMPONENTS
// =============================================================================
// M3ListRow.kt          - List row with icon, label, description, trailing content
// M3ListComponents.kt   - Enhanced list components (deprecated, use individual files)
// Cards.kt              - DCCard (animated card with label), DCCardRow

// =============================================================================
// APP BAR & NAVIGATION
// =============================================================================
// M3Common.kt           - Core app components:
//                         - DCBackButton, DCAppBarAction, DCAppBarZone
//                         - rememberCollapsibleAppBarState
//                         - DCAddFab, DCLoadingBox, DCEmptyState
//                         - DCTopAppBarTitle, rememberSnackbarState
//                         - M3SnackbarHost
// DateNavigationBar.kt  - Date picker navigation with prev/next buttons
// CircleActionButton.kt - Simple circular action button

// =============================================================================
// DIALOGS
// =============================================================================
// M3Dialog.kt           - Dialog components:
//                         - DCConfirmDialog, DCAlertDialog
// FormDialogs.kt        - Form dialog components:
//                         - DCPickerDialog, DCFormDialog
// MultiPageDialog.kt    - DCMultiPageDialog (wizard-style multi-step dialog)

// =============================================================================
// SELECTORS & INPUTS
// =============================================================================
// SegmentedSelector.kt  - M3SegmentedSelector (animated segmented control)
// FormFields.kt         - Form field components:
//                         - DCTextField, DCCurrencyField, DCSwitchRow,
//                         - DCDropdownField, DCDateField, DCFieldLabel
// FormControls.kt       - Input control components:
//                         - DCSelectionChip, DCQuantityStepper, DCTextChip,
//                         - DCActionChip, DCTabSelector, DCAnimatedActionRow

// =============================================================================
// SHEETS
// =============================================================================
// SheetComponents.kt    - Bottom sheet components:
//                         - DCOptionsSheet, DCSheetOptionRow

// =============================================================================
// EXPRESSIVE COMPONENTS
// =============================================================================
// M3Expressive.kt       - Visual components:
//                         - M3TonalActionButton, M3TonalChip, M3ProfileBanner,
//                         - M3AmountDisplay, CurrencyShimmer, DashboardSectionBody,
//                         - DashboardEmptyBox

// =============================================================================
// UTILITY & FEEDBACK
// =============================================================================
// ShimmerEffect.kt      - Loading shimmer placeholder effect
// NoDataPlaceholder.kt  - Empty state with icon and message
// SectionHeader.kt      - Section header with optional action button
// DateNavigation.kt     - Month stepping utilities (stepMonth, stepMonth0)
// Formatters.kt         - Currency and date formatting utilities

// =============================================================================
// COLORS & THEMING
// =============================================================================
// M3Colors.kt           - Complete color palette:
//                         - M3Primary, M3GreenColor, M3RedColor, etc.
//                         - Container colors, status colors
//                         - Transaction type colors (Booking, Goods, Expense)
//                         - Status colors (Remitted, Unremitted, Pending)
//                         - Month color palette
//                         - Layout spacing constants

// =============================================================================
// THEME ACCESS (from theme package)
// =============================================================================
// theme.DeutschCraftTheme provides:
//   - spacing: Spacing (xs, sm, md, lg, xl, xxl)
//   - fontSize: FontSize (xxxs through displayUltra)
//   - shapes: Shapes for MaterialTheme

// Usage example:
//   DeutschCraftTheme.spacing.lg     // 16.dp
//   DeutschCraftTheme.fontSize.heading // 20.sp
