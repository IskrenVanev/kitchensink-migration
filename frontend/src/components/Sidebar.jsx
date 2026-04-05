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
      </ul>
    </div>
  );
}
