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

## Role-Based Warehouse Workflow

The application is expanding from an admin-focused rule simulation system into a role-based warehouse workflow. This workflow is a business layer around the existing rule engine. It does not replace the Interpreter Pattern or Strategy Pattern architecture.

### Admin

Admin has full system access and is responsible for system configuration, rule management, and strategy management.

Admin responsibilities:

* Create, edit, delete, enable, and disable rules.
* Configure rule conditions evaluated by the Interpreter Pattern.
* Configure or select strategies used by the Strategy Pattern.
* Manage robots.
* View dashboard, rules, robots, simulation, system flow, and execution history.

Admin rule creation connects directly to the technical rule engine. Admin-configured conditions become expressions evaluated by the Interpreter Pattern. When a rule matches, its configured strategy name is passed to the Strategy Pattern for robot behavior execution.

### Manager

Manager is responsible for warehouse operation control, not complex rule creation.

Manager responsibilities:

* Use rules or policies already created by Admin.
* Assign existing rule sets or policies to suitable robots or warehouse zones.
* Monitor robot status.
* Decide which robots are available for operation.
* Manage robot availability, zone assignment, and rule or policy assignment.

Manager assignment connects rule configuration to daily robot operation. The Manager does not redefine the rule engine; the Manager chooses which existing rules or policies should apply to specific robots or zones.

### Staff

Staff interacts with the system from an operational and customer-service perspective.

Staff responsibilities:

* Handle customer pickup requests.
* Create a pickup request when a customer needs an item.
* Select or enter cargo type, cargo location, warehouse zone, and priority.
* Track mission status until the pickup mission is completed.

A Staff pickup request starts the business workflow. The system uses the request data and available robot state to select a suitable robot, then passes robot and mission data into the existing rule engine.

### Business Flow Around the Rule Engine

The original technical flow remains:

```text
Robot Input -> Interpreter -> Rule Match -> Strategy Dispatch -> Robot Action -> History
```

The role-based business flow wraps that technical core:

```text
Staff Pickup Request
-> Robot Selection
-> Rule/Policy Assignment from Manager
-> Interpreter Evaluation
-> Strategy Dispatch
-> Robot Action
-> Live Map / Mission Status
-> History
```

Full planned flow:

1. Admin creates and manages rules and strategies.
2. Manager assigns existing rule sets or policies to robots or zones.
3. Staff creates a pickup request when a customer needs cargo.
4. The system finds a suitable available robot.
5. The system reads robot state such as battery, obstacleDetected, robotLoad, distance, and priority.
6. The Interpreter Pattern evaluates active rules based on robot and mission data.
7. The highest-priority matched rule is selected.
8. The Strategy Pattern dispatches the selected strategy.
9. The system generates the final robot action.
10. The mission status is updated.
11. The live warehouse map can visualize robot movement through Zone A, Zone B, or Zone C.
12. Execution history is saved for dashboard, history, and system-flow review.

## Pattern Responsibilities

The Interpreter Pattern evaluates rule conditions. It checks supported fields such as battery, obstacleDetected, robotLoad, distance, and priority using supported operators such as `<`, `>`, `==`, `<=`, `>=`, `AND`, and `OR`.

The Strategy Pattern executes the selected robot behavior. After the rule engine selects the highest-priority matched rule, the configured strategy is dispatched to produce the final robot action.

The role workflow explains who creates requests, who assigns operational policies, and who configures rules. The rule engine remains the technical core of the project.

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
