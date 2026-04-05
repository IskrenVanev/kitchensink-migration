# Frontend Integration: JSF to React (Vite)

## Migration background

The legacy kitchensink application used **JavaServer Faces (JSF)** — a server-side templating
technology where the server rendered HTML directly and sent it to the browser. Every interaction
triggered a round-trip to the server. The JSF views (`index.xhtml`, `default.xhtml`) mixed HTML
markup, data binding expressions, and Java EL directly in the template files.

When the backend was migrated to Spring Boot, JSF was dropped entirely. Spring Boot does not
provide JSF support out of the box. The frontend therefore had to be replaced with something
compatible with a pure REST backend that returns JSON.

The migration went through two phases:

1. **Phase 1 — CDN React (interim):** A single `app.jsx` file was placed in
   `src/main/resources/static/js/` and loaded via `<script type="text/babel">`. React and the
   Babel JSX transformer were loaded at runtime from CDN links. This worked immediately without
   any build tooling, but was not suitable for a production or maintainable codebase: every page
   load downloaded a full compiler from the internet, JSX was transformed in the browser on every
   visit, and the entire application lived in one 178-line file with no module separation.

2. **Phase 2 — Vite + React (current):** The CDN/Babel approach was replaced with a proper
   build pipeline. React source is written in a dedicated `frontend/` directory, compiled ahead of
   time by Vite into a single optimised bundle, and that bundle is placed in
   `src/main/resources/static/js/app.js` for Spring Boot to serve. This is the industry-standard
   approach for embedding a React frontend inside a Java web application.

---

## How the two-folder structure works

There are two distinct directories involved, and understanding their roles is essential:

```
frontend/                            ← Developer writes React source code here
  index.html                         ← Entry HTML used ONLY by the Vite dev server
  src/
    main.jsx                         ← React entry point (mounts app to #app div)
    App.jsx                          ← Root component — owns all state
    components/
      MemberForm.jsx                 ← Registration form + inline validation display
      MemberTable.jsx                ← Members list table
      Sidebar.jsx                    ← Technologies sidebar with Spring Boot logo
      StatusMessages.jsx             ← Success / error message display
    services/
      memberApi.js                   ← All HTTP calls to /rest/members

src/main/resources/static/          ← Spring Boot serves everything from here
  index.html                         ← The real HTML page the browser loads
  js/
    app.js                           ← ← ← Vite BUILD OUTPUT — do not edit by hand
  css/
    screen.css                       ← Stylesheet (served as a static file)
  gfx/
    springBoot.png                   ← Sidebar logo
    modernIcon.jpg                   ← Browser favicon
```

`vite.config.js` is the bridge between the two sides. Running `npm run build` reads
`frontend/src/main.jsx`, compiles all JSX, resolves all imports, bundles React and the
application code together, minifies the result, and writes it to
`src/main/resources/static/js/app.js`.

---

## What `app.js` is

`src/main/resources/static/js/app.js` is the **compiled and bundled output** of the entire React
application. It is a plain JavaScript file that a browser can execute directly without any
additional tools.

Before the Vite build existed, browsers could not understand JSX syntax (`<App />`,
`<div className="...">`, etc.) because JSX is not valid JavaScript. Vite compiles each `.jsx`
file into plain JavaScript function calls, resolves all `import` statements, and concatenates
everything — including the React library itself — into one file. The result is a self-contained
bundle.

**You never edit `app.js` by hand.** It is regenerated every time you run `npm run build`. If
you need to change the UI, edit the source files in `frontend/src/` and rebuild.

The file is loaded by `src/main/resources/static/index.html` as a JavaScript module:

```html
<script type="module" src="/js/app.js"></script>
```

When the browser loads the page, it downloads `app.js`, executes it, and the last line in the
bundle calls `createRoot(document.getElementById("app")).render(...)` — which mounts the React
application into the `<div id="app">` placeholder in `index.html`.

---

## Component breakdown

### `frontend/src/main.jsx`
The entry point. Calls `ReactDOM.createRoot` to mount the React tree into the `#app` div. This
is the file Vite uses as the build entry point.

### `frontend/src/App.jsx`
The root component. Owns all application state:
- `members` — the list fetched from `GET /rest/members`
- `form` — controlled input values (name, email, phoneNumber)
- `fieldErrors` — per-field validation messages from `400` responses
- `globalMessage` / `globalError` — success and error banners

On mount, `useEffect` triggers a `getMembers()` call to populate the table. On form submit,
`createMember()` is called, and the response is inspected to either show success, set field
errors, or show a global error. On success, the form is reset and `loadMembers()` is called
again to refresh the table.

### `frontend/src/components/MemberForm.jsx`
Renders the registration form. Receives form state and callbacks as props. Displays field-level
error messages below each input using `<span className="invalid">`. Delegates to
`StatusMessages` for global messages.

### `frontend/src/components/MemberTable.jsx`
Renders the members table using the `.simpletablestyle` CSS class. Maps over the `members` array
and renders one row per member. Each row includes a clickable REST URL link to
`/rest/members/{id}`.

### `frontend/src/components/Sidebar.jsx`
Renders the aside panel with the Spring Boot logo and links to Spring documentation. Stateless.

### `frontend/src/components/StatusMessages.jsx`
Renders the global success or error message below the form. Shown only when a message is present.

### `frontend/src/services/memberApi.js`
Encapsulates all HTTP calls to the backend. Two exported functions:
- `getMembers()` — `GET /rest/members`, returns the parsed JSON array.
- `createMember(payload)` — `POST /rest/members` with JSON body, returns an object with `ok`,
  `status`, and `errorPayload` so the caller can handle all response cases without needing to
  touch `fetch` directly.

Keeping API calls in a dedicated service module means that if the backend URL scheme changes,
only this file needs updating — no component code changes required.

---

## How to start the application

### Production mode (normal use)

This is how the application is run during a demo or for any real usage. Spring Boot serves
everything — the HTML page, the CSS, images, and the pre-built JavaScript bundle.

**Prerequisites:** Java 21, Maven (or use the included `mvnw.cmd` wrapper). Node.js is
only needed if you want to rebuild the frontend.

```bash
# From the project root:
./mvnw.cmd spring-boot:run
```

Then open: **http://localhost:8081**

Spring Boot finds `src/main/resources/static/index.html` and registers it as the welcome page.
The browser loads it, which in turn loads `/js/app.js` (the built React bundle),
`/css/screen.css`, and `/gfx/modernIcon.jpg`. The React app then boots and calls
`GET /rest/members` to populate the table.

### After changing React source code

If you edit any file in `frontend/src/`, you must rebuild the bundle before the changes appear
in production mode:

```bash
npm install        # only needed once, or after package.json changes
npm run build      # compiles frontend/src/ → src/main/resources/static/js/app.js
./mvnw.cmd spring-boot:run
```

### Development mode (hot reload while editing React)

Run both servers simultaneously. Vite provides instant hot reload on file save; Spring Boot
provides the REST API, CSS, and images.

Terminal 1:
```bash
./mvnw.cmd spring-boot:run
```

Terminal 2:
```bash
npm run dev
```

Then open: **http://localhost:5173**

The Vite dev server proxies all `/rest`, `/css`, and `/gfx` requests to `http://localhost:8081`
(configured in `vite.config.js`). Changes to `.jsx` files appear in the browser immediately
without a rebuild or page reload.

---

## Why `screen.css` was kept as a plain CSS file

This is an intentional engineering decision rooted in the migration risk model, not an
oversight.

The challenge brief explicitly asks the migration to be approached **as if the codebase were far
larger** — applying the kind of infrastructure thinking, risk management, and incremental change
discipline that a real large-scale migration requires.

In a real large migration, CSS is a significant risk area:

- **Scope risk:** CSS class names and selectors are used across many components. Changing the
  styling system (e.g. switching from a flat CSS file to CSS Modules or a component library) is
  an independent workstream that can break visual regression in unexpected ways.
- **Separation of concerns:** Mixing a CSS system migration into a JS architecture migration
  compounds risk. If something breaks, it is harder to determine whether the cause was the
  component refactoring or the styling change.
- **Incremental approach:** The right strategy in a large migration is to move one axis at a
  time. The first pass establishes the React component architecture and gets it shipping. A
  separate, later pass migrates the styling system (e.g. to CSS Modules or Tailwind), with its
  own testing and review cycle.

For this project, `screen.css` was **adapted** from the legacy JSF stylesheet — CSS variables
were introduced, legacy JSF-specific selectors were removed, and the visual design was
modernised — while keeping it as a single file served statically. The React components reference
the same class names (`simpletablestyle`, `form-row`, `register`, etc.) that the legacy JSF
templates used, ensuring visual continuity and making the migration auditable.

The result is that the CSS layer is a known, stable baseline. Future work to componentise the
styling (e.g. extracting each component's styles into a co-located `.module.css` file) can be
done as its own change without touching component logic.

---

## Behavior parity with the original JSF frontend

| Legacy JSF behavior | React implementation |
|---|---|
| Page load shows member list | `useEffect` calls `GET /rest/members` on mount |
| Form submits to JSF backing bean | `onSubmit` calls `POST /rest/members` with JSON |
| Validation errors shown per field | `400` response body mapped to `fieldErrors` state |
| Duplicate email shown as error | `409` response sets `fieldErrors.email = "Email taken"` |
| Success clears form and reloads list | `form` reset to empty, `loadMembers()` called again |
| REST URL column links to member endpoint | `<a href="/rest/members/{id}">` rendered in table |

---

## Build pipeline diagram

```
npm run build
     │
     ▼
 vite.config.js
  root: "frontend"
  input: frontend/src/main.jsx
  outDir: src/main/resources/static/js
     │
     ▼
 Vite (Rollup)
  - Resolves all imports
  - Compiles JSX → plain JS
  - Bundles React + app code
  - Minifies (production mode)
     │
     ▼
 src/main/resources/static/js/app.js   (~146 KB gzipped: ~47 KB)
     │
     ▼
 Spring Boot static resource handler
  serves /js/app.js at http://localhost:8081/js/app.js
     │
     ▼
 Browser loads index.html → downloads app.js → React mounts → app runs
```

---

## Suggested Commit Message
```text
feat: migrate frontend to Vite React architecture with UI parity

- Replace CDN React/Babel runtime setup with Vite build pipeline
- Refactor monolithic app.jsx into modular components and API service layer
- Keep legacy UI layout and class names for CSS continuity and audit trail
- Generate production bundle to src/main/resources/static/js/app.js
- Preserve Spring Boot static serving; no changes to backend required
- Fix Vite lib-mode process.env bug by switching to app-mode rollupOptions.input
```
