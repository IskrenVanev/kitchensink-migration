import React from "react";

export default function MemberTable({ members }) {
  return (
    <>
      <table className="simpletablestyle">
        <thead>
          <tr>
            <th>Id</th>
            <th>Name</th>
            <th>Email</th>
            <th>Phone #</th>
            <th>REST URL</th>
          </tr>
        </thead>
        <tbody>
          {members.map((member) => (
            <tr key={member.id}>
              <td>{member.id}</td>
              <td>{member.name}</td>
              <td>{member.email}</td>
              <td>{member.phoneNumber}</td>
              <td>
                <a href={`/rest/members/${member.id}`}>{`/rest/members/${member.id}`}</a>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <p>
        REST URL for all members: <a href="/rest/members">/rest/members</a>
      </p>
    </>
  );
}
