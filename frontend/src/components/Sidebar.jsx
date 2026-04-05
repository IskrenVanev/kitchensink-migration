import React from "react";

export default function Sidebar() {
  return (
    <div id="aside">
      <img src="/gfx/springBoot.png" className="aside-logo" alt="Spring Boot" />
      <p>Learn more about the technologies used in this modernized project.</p>
      <ul>
        <li>
          <a href="https://spring.io/projects/spring-boot">
            Spring Boot Documentation
          </a>
        </li>
        <li>
          <a href="https://spring.io/guides">
            Spring Guides
          </a>
        </li>
        <li>
          <a href="https://react.dev/versions">
            React Versions (v19.2.4 is used in this project)
          </a>
        </li>
        <li>
          <a href="https://vite.dev/">
            Vite Documentation (v8.0.3 is used in this project)
          </a>
        </li>
      </ul>
    </div>
  );
}
