import React from "react";
import StatusMessages from "./StatusMessages";

export default function MemberForm({
  form,
  fieldErrors,
  onInputChange,
  onSubmit,
  globalMessage,
  globalError
}) {
  return (
    <form id="reg" onSubmit={onSubmit} noValidate>
      <h2>Member Registration</h2>
      <p>Enforces annotation-based constraints defined on the model class.</p>

      <div className="form-row">
        <label htmlFor="name">Name:</label>
        <input id="name" name="name" value={form.name} onChange={onInputChange} />
        {fieldErrors.name && <span className="invalid">{fieldErrors.name}</span>}
      </div>

      <div className="form-row">
        <label htmlFor="email">Email:</label>
        <input id="email" name="email" value={form.email} onChange={onInputChange} />
        {fieldErrors.email && <span className="invalid">{fieldErrors.email}</span>}
      </div>

      <div className="form-row">
        <label htmlFor="phoneNumber">Phone #:</label>
        <input id="phoneNumber" name="phoneNumber" value={form.phoneNumber} onChange={onInputChange} />
        {fieldErrors.phoneNumber && <span className="invalid">{fieldErrors.phoneNumber}</span>}
      </div>

      <div className="actions">
        <button id="register" className="register" type="submit">Register</button>
      </div>

      <StatusMessages globalMessage={globalMessage} globalError={globalError} />
    </form>
  );
}
