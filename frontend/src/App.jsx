import React, { useEffect, useState } from "react";
import MemberForm from "./components/MemberForm";
import MemberTable from "./components/MemberTable";
import Sidebar from "./components/Sidebar";
import { createMember, getMembers } from "./services/memberApi";

const EMPTY_FORM = {
  name: "",
  email: "",
  phoneNumber: ""
};

export default function App() {
  const [members, setMembers] = useState([]);
  const [form, setForm] = useState(EMPTY_FORM);
  const [fieldErrors, setFieldErrors] = useState({});
  const [globalMessage, setGlobalMessage] = useState("");
  const [globalError, setGlobalError] = useState("");

  async function loadMembers() {
    const loadedMembers = await getMembers();
    setMembers(loadedMembers);
  }

  useEffect(() => {
    loadMembers().catch((error) => {
      setGlobalError(error.message || "Failed to load members");
    });
  }, []);

  function onInputChange(event) {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function onSubmit(event) {
    event.preventDefault();
    setFieldErrors({});
    setGlobalMessage("");
    setGlobalError("");

    try {
      const result = await createMember(form);

      if (result.ok) {
        setForm(EMPTY_FORM);
        setGlobalMessage("Member registered successfully.");
        await loadMembers();
        return;
      }

      if (result.status === 400 && result.errorPayload && typeof result.errorPayload === "object") {
        setFieldErrors(result.errorPayload);
        return;
      }

      if (result.status === 409) {
        setFieldErrors({ email: "Email taken" });
        return;
      }

      setGlobalError(result.errorPayload.error || "Registration failed");
    } catch (error) {
      setGlobalError(error.message || "Registration failed");
    }
  }

  return (
    <div id="container">
      <div id="content">
        <h1>Welcome to Kitchensink Modernized</h1>
        <div>
          <p>You are running the Spring Boot modernization of the original Kitchensink application.</p>
        </div>

        <MemberForm
          form={form}
          fieldErrors={fieldErrors}
          onInputChange={onInputChange}
          onSubmit={onSubmit}
          globalMessage={globalMessage}
          globalError={globalError}
        />

        <h2>Members</h2>
        {members.length === 0 ? (
          <em>No registered members.</em>
        ) : (
          <MemberTable members={members} />
        )}
      </div>

      <Sidebar />

      <div id="footer">
        <p>
          Modernization challenge: Jakarta EE Kitchensink migrated to Spring Boot.
          <br />
        </p>
      </div>
    </div>
  );
}
