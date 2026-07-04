---
name: Smart Warehouse Robotics Decision Engine
colors:
  surface: '#f8f9ff'
  surface-dim: '#d0dbed'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e6eeff'
  surface-container-high: '#dee9fc'
  surface-container-highest: '#d9e3f6'
  on-surface: '#121c2a'
  on-surface-variant: '#424754'
  inverse-surface: '#27313f'
  inverse-on-surface: '#eaf1ff'
  outline: '#727785'
  outline-variant: '#c2c6d6'
  surface-tint: '#005ac2'
  primary: '#0058be'
  on-primary: '#ffffff'
  primary-container: '#2170e4'
  on-primary-container: '#fefcff'
  inverse-primary: '#adc6ff'
  secondary: '#5c5f60'
  on-secondary: '#ffffff'
  secondary-container: '#dee0e2'
  on-secondary-container: '#606365'
  tertiary: '#924700'
  on-tertiary: '#ffffff'
  tertiary-container: '#b75b00'
  on-tertiary-container: '#fffbff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d8e2ff'
  primary-fixed-dim: '#adc6ff'
  on-primary-fixed: '#001a42'
  on-primary-fixed-variant: '#004395'
  secondary-fixed: '#e1e2e4'
  secondary-fixed-dim: '#c5c6c8'
  on-secondary-fixed: '#191c1e'
  on-secondary-fixed-variant: '#444749'
  tertiary-fixed: '#ffdcc6'
  tertiary-fixed-dim: '#ffb786'
  on-tertiary-fixed: '#311400'
  on-tertiary-fixed-variant: '#723600'
  background: '#f8f9ff'
  on-background: '#121c2a'
  surface-variant: '#d9e3f6'
typography:
  headline-lg:
    fontFamily: Inter
    fontSize: 30px
    fontWeight: '700'
    lineHeight: 38px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
  code:
    fontFamily: monospace
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  base: 4px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 40px
  container-max: 1200px
  gutter: 24px
---

## Brand & Style

The brand personality for this design system is academic, approachable, and methodical. It is tailored for a student-led software engineering project, prioritizing clarity and logical flow over marketing flair. The UI should evoke a sense of organized intelligence—like a well-structured textbook or a modern open-source documentation site.

The chosen style is **Minimalism** with a **Corporate / Modern** influence. It utilizes a vast amount of white space to reduce cognitive load, helping users focus on complex algorithmic data without distraction. Interactions are predictable and grounded, ensuring the "Decision Engine" feels reliable and easy to audit.

## Colors

This design system uses a restricted palette to maintain a clean, educational aesthetic.

*   **Primary (#3B82F6):** A soft blue used exclusively for primary actions (buttons), active states, and highlighting key data insights.
*   **Neutral-Dark (#1F2937):** Used for all primary text to ensure high contrast and accessibility.
*   **Neutral-Light (#E5E7EB):** Specifically for structural borders and dividers to separate content without adding visual weight.
*   **Background (#FFFFFF):** The entire application sits on a pure white canvas to maximize the feeling of "cleanliness" and modernity.
*   **Surface-Muted (#F9FAFB):** An optional very light tint for table headers or secondary background areas to provide subtle nesting.

## Typography

The system relies on **Inter**, a highly legible sans-serif font designed for screens. It provides a "system-native" feel that aligns with modern React and Spring Boot administrative dashboards.

*   **Headlines:** Use a tight letter-spacing and heavier weights to establish clear hierarchy.
*   **Body Text:** Standard weight for maximum readability. Line heights are generous to prevent the "wall of text" effect often found in technical projects.
*   **Monospace:** For robotics coordinates, engine logs, or data snippets, a standard system monospace font is used to denote technical data.

## Layout & Spacing

The layout follows a **Fixed Grid** philosophy for the main content area, centering the "Engine" interface on the screen to maintain focus.

*   **Grid:** A 12-column system is used for dashboard layouts. On desktop, the content is capped at 1200px.
*   **Padding:** Use `md` (16px) for internal component padding and `lg` (24px) for spacing between major sections or cards.
*   **Mobile Adaption:** For mobile devices, the 12-column grid collapses into a single column. Horizontal margins reduce from 40px to 16px to maximize available screen real estate.

## Elevation & Depth

This design system avoids heavy shadows and complex layering. It uses **Low-contrast outlines** and **Ambient shadows** to create a shallow, organized depth.

*   **Level 0 (Flat):** The main application background.
*   **Level 1 (Cards):** Surfaces use a 1px border (#E5E7EB) and a very subtle, diffused shadow (0px 1px 3px rgba(0,0,0,0.1)) to lift them slightly off the background.
*   **Interactions:** Hover states on buttons or cards should not change elevation; instead, use a slight color shift or a more defined border color to indicate interactivity.

## Shapes

The shape language is **Soft** and restrained. This choice strikes a balance between the precision required for a "Robotics Engine" and the friendliness required for an "Educational Web App."

*   **Components:** Buttons, input fields, and small cards use a 0.25rem (4px) corner radius.
*   **Large Containers:** Main content blocks or modal windows use 0.5rem (8px) for a more modern, approachable feel.
*   **Strictness:** Avoid pill-shaped or fully rounded elements to maintain a professional, structured appearance.

## Components

### Buttons
Primary buttons use a solid `#3B82F6` background with white text. Secondary buttons use a white background with a `#E5E7EB` border and `#1F2937` text. Use consistent 4px rounding.

### Cards
Cards are the primary container for robotics data. They feature a white background, 1px border (#E5E7EB), and a subtle shadow. Card headers should have a thin bottom border to separate the title from the content.

### Tables
Tables are essential for displaying warehouse inventory and decision logs. They should be borderless on the sides, using only horizontal dividers. The header row should have a subtle gray background (`#F9FAFB`) and bolded labels.

### Form Inputs
Inputs use a standard height (40px) with a 1px border. When focused, the border color changes to the primary blue (#3B82F6) with a faint blue glow (ring).

### Status Badges
To indicate robotics statuses (e.g., "Moving," "Idle," "Error"), use small chips with light background tints and darker text. For example, a "Success" state would use a very light green background and dark green text, rather than the primary blue.

### Logic Visualizers
As a "Decision Engine," include simple horizontal progress bars or step-indicators to show the engine's current processing phase. These should use the primary blue for completion.