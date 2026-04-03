# Material Icons Reference for DeutschCraft

This document catalogs all Material Design icons used in the DeutschCraft application and their appropriate usage contexts.

## Setup

Icons are provided by the `compose.materialIconsExtended` dependency for Compose Multiplatform Material 3.

```kotlin
// build.gradle.kts
implementation(compose.materialIconsExtended)
```

## Import Statement

```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.[IconName]
import androidx.compose.material.icons.outlined.[IconName]
```

## Icons Currently in Use

### Action Icons

| Icon | Import Path | Usage Context | File Location |
|------|-------------|---------------|---------------|
| **Add** | `Icons.Filled.Add` | Add/append content | `SuggestionsContent.kt` |
| **AddCircle** | `Icons.Filled.AddCircle` | Continue/suggest more action | `SuggestionsContent.kt` |
| **Check** | `Icons.Filled.Check` | Apply/confirm action | `SuggestionsContent.kt` |
| **CheckCircle** | `Icons.Filled.CheckCircle` | Grammar check action | `SuggestionsContent.kt` |
| **Clear/Close** | `Icons.Filled.Clear` or `Icons.Filled.Close` | Dismiss/clear content | Multiple files |
| **Edit** | `Icons.Filled.Edit` | Improve/edit action | `SuggestionsContent.kt`, `ContextualActions.kt` |
| **Refresh** | `Icons.Filled.Refresh` | Rephrase action | `SuggestionsContent.kt`, `ContextualActions.kt` |
| **Send** | `Icons.Filled.Send` | Send message action | `ChatPanel.kt` |
| **ArrowBack** | `Icons.Filled.ArrowBack` | Navigate back | Multiple files |
| **ArrowForward** | `Icons.Filled.ArrowForward` | Navigate forward | Multiple files |
| **ArrowDropDown** | `Icons.Filled.ArrowDropDown` | Dropdown indicator | `FormFields.kt` |

### Navigation Icons

| Icon | Import Path | Usage Context | File Location |
|------|-------------|---------------|---------------|
| **KeyboardArrowLeft** | `Icons.Filled.KeyboardArrowLeft` | Previous page/item | `DateNavigationBar.kt`, `MultiPageDialog.kt` |
| **KeyboardArrowRight** | `Icons.Filled.KeyboardArrowRight` | Next page/item | `DateNavigationBar.kt`, `MultiPageDialog.kt` |

### Status/Information Icons

| Icon | Import Path | Usage Context | File Location |
|------|-------------|---------------|---------------|
| **Info** | `Icons.Filled.Info` or `Icons.Outlined.Info` | Information/help | `SuggestionsContent.kt`, `M3Dialog.kt` |
| **Warning** | `Icons.Filled.Warning` | Warning/caution | `NoDataPlaceholder.kt`, `M3Badges.kt` |
| **Star** | `Icons.Filled.Star` | AI suggestion/generic fallback | `SuggestionsContent.kt` |
| **DateRange** | `Icons.Filled.DateRange` | Date picker | `FormFields.kt`, `DateNavigationBar.kt` |

### UI Component Icons

| Icon | Import Path | Usage Context | File Location |
|------|-------------|---------------|---------------|
| **Settings** | `Icons.Filled.Settings` | Settings/configuration | `App.kt` |
| **Build** | `Icons.Filled.Build` | Developer tools | `App.kt` |
| **Email** | `Icons.Filled.Email` | Email/contact | `FormFields.kt`, `ContextualActions.kt` |
| **Delete** | `Icons.Filled.Delete` | Delete action | `ContextualActions.kt` |

## Icon Categories by Feature

### Suggestions Panel
- `CheckCircle` - Grammar check
- `Edit` - Improve text
- `Refresh` - Rephrase text
- `AddCircle` - Continue/suggest more
- `Star` - Generic AI suggestion
- `Check` - Apply suggestion
- `Add` - Use/append suggestion

### Chat Panel
- `Send` - Send message
- `Info` - Information/help

### Editor
- `Settings` - Editor settings
- `Build` - Developer tools

### Forms
- `ArrowDropDown` - Dropdown fields
- `DateRange` - Date pickers
- `Email` - Email fields
- `KeyboardArrowLeft/Right` - Navigation

## Migration Notes

### From Material 2 to Material 3 Icons

**Before (Material 2 - ERROR):**
```kotlin
import androidx.compose.material.icons.Icons  // Wrong import
import androidx.compose.material.icons.filled.Add
```

**After (Material 3 - CORRECT):**
```kotlin
import androidx.compose.material.icons.Icons  // Same import path works
import androidx.compose.material.icons.filled.Add  // Add import
```

**Note:** The import paths look the same, but the `compose.materialIconsExtended` dependency is required for the icons to resolve properly in Compose Multiplatform Material 3 projects.

## Common Icon Patterns

### Action Button with Icon
```kotlin
Button(onClick = onAction) {
    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
    Spacer(modifier = Modifier.width(4.dp))
    Text("Apply")
}
```

### Icon-only Button
```kotlin
IconButton(onClick = onDismiss) {
    Icon(Icons.Filled.Close, contentDescription = "Close")
}
```

### Status Indicator with Tint
```kotlin
Icon(
    imageVector = Icons.Filled.Info,
    contentDescription = null,
    tint = Gray400,
    modifier = Modifier.size(24.dp)
)
```

## Available Material Icon Sets

Material Icons Extended provides the following icon sets:

- **Filled** - `Icons.Filled.*` (solid icons, highest emphasis)
- **Outlined** - `Icons.Outlined.*` (stroke-based, medium emphasis)
- **Rounded** - `Icons.Rounded.*` (rounded corners)
- **Sharp** - `Icons.Sharp.*` (sharp corners)
- **TwoTone** - `Icons.TwoTone.*` (dual-color)

### Recommendation
Use **Filled** icons for primary actions and interactive elements, and **Outlined** icons for secondary/informational content.

## Full Icon Catalog

The complete list of 2000+ Material Design icons is available at:
https://fonts.google.com/icons

When using an icon from the catalog, the Kotlin import name usually matches the icon name in kebab-case to PascalCase:
- Icon name: "check-circle" → Import: `Icons.Filled.CheckCircle`
- Icon name: "keyboard-arrow-left" → Import: `Icons.Filled.KeyboardArrowLeft`
