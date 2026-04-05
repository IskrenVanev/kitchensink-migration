import React from "react";
import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import MemberTable from "../components/MemberTable";

const sampleMembers = [
  { id: 1, name: "Alice", email: "alice@example.com", phoneNumber: "2125551212" },
  { id: 2, name: "Bob", email: "bob@example.com", phoneNumber: "9876543210" },
];

describe("MemberTable", () => {
  it("renders all five column headers", () => {
    render(<MemberTable members={sampleMembers} />);
    ["Id", "Name", "Email", "Phone #", "REST URL"].forEach((header) => {
      expect(screen.getByText(header)).toBeInTheDocument();
    });
  });

  it("renders a row for each member", () => {
    render(<MemberTable members={sampleMembers} />);
    expect(screen.getByText("Alice")).toBeInTheDocument();
    expect(screen.getByText("Bob")).toBeInTheDocument();
  });

  it("renders the email for each member", () => {
    render(<MemberTable members={sampleMembers} />);
    expect(screen.getByText("alice@example.com")).toBeInTheDocument();
    expect(screen.getByText("bob@example.com")).toBeInTheDocument();
  });

  it("renders the phone number for each member", () => {
    render(<MemberTable members={sampleMembers} />);
    expect(screen.getByText("2125551212")).toBeInTheDocument();
    expect(screen.getByText("9876543210")).toBeInTheDocument();
  });

  it("renders a REST URL link for each member pointing to /rest/members/{id}", () => {
    render(<MemberTable members={sampleMembers} />);

    const link1 = screen.getByRole("link", { name: "/rest/members/1" });
    expect(link1).toBeInTheDocument();
    expect(link1).toHaveAttribute("href", "/rest/members/1");

    const link2 = screen.getByRole("link", { name: "/rest/members/2" });
    expect(link2).toBeInTheDocument();
    expect(link2).toHaveAttribute("href", "/rest/members/2");
  });

  it("renders the all-members REST URL link", () => {
    render(<MemberTable members={sampleMembers} />);
    const link = screen.getByRole("link", { name: "/rest/members" });
    expect(link).toBeInTheDocument();
    expect(link).toHaveAttribute("href", "/rest/members");
  });

  it("renders the member id in the Id column", () => {
    render(<MemberTable members={[{ id: 42, name: "Dave", email: "dave@example.com", phoneNumber: "2125551212" }]} />);
    expect(screen.getByText("42")).toBeInTheDocument();
  });

  it("renders an empty tbody when members list is empty", () => {
    render(<MemberTable members={[]} />);
    expect(screen.queryByRole("row", { name: /alice/i })).not.toBeInTheDocument();
    expect(screen.getByText("Id")).toBeInTheDocument();
  });

  it("renders exactly one row per member", () => {
    render(<MemberTable members={sampleMembers} />);
    const rows = screen.getAllByRole("row");
    expect(rows).toHaveLength(sampleMembers.length + 1);
  });
});
