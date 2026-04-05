import React from "react";
import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import Sidebar from "../components/Sidebar";

describe("Sidebar", () => {
  it("renders the Spring Boot logo image", () => {
    render(<Sidebar />);
    expect(screen.getByAltText("Spring Boot")).toBeInTheDocument();
  });

  it("renders the Spring Boot Documentation link", () => {
    render(<Sidebar />);
    const link = screen.getByRole("link", { name: /spring boot documentation/i });
    expect(link).toBeInTheDocument();
    expect(link).toHaveAttribute("href", "https://spring.io/projects/spring-boot");
  });

  it("renders the Spring Guides link", () => {
    render(<Sidebar />);
    const link = screen.getByRole("link", { name: /spring guides/i });
    expect(link).toBeInTheDocument();
    expect(link).toHaveAttribute("href", "https://spring.io/guides");
  });

  it("renders the React Versions link", () => {
    render(<Sidebar />);
    const link = screen.getByRole("link", { name: /react versions/i });
    expect(link).toBeInTheDocument();
    expect(link).toHaveAttribute("href", "https://react.dev/versions");
  });

  it("renders the Vite Documentation link", () => {
    render(<Sidebar />);
    const link = screen.getByRole("link", { name: /vite documentation/i });
    expect(link).toBeInTheDocument();
    expect(link).toHaveAttribute("href", "https://vite.dev/");
  });

  it("renders the learn more introduction text", () => {
    render(<Sidebar />);
    expect(
      screen.getByText(/learn more about the technologies used in this modernized project/i)
    ).toBeInTheDocument();
  });

  it("shows React version in the link text", () => {
    render(<Sidebar />);
    expect(screen.getByText(/v19\.2\.4 is used in this project/i)).toBeInTheDocument();
  });

  it("shows Vite version in the link text", () => {
    render(<Sidebar />);
    expect(screen.getByText(/v8\.0\.3 is used in this project/i)).toBeInTheDocument();
  });
});
