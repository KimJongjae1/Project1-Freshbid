import React from "react";
import { useParams, useSearchParams } from "react-router-dom";

import LiveHostView from "./LiveHostView";
import LiveParticipantView from "./LiveParticipantView";

const LiveRoom: React.FC = () => {
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const role = searchParams.get("type");

  if (!id || (role !== "host" && role !== "participant")) {
    return <p>잘못된 접근입니다.</p>;
  }

  return role === "host" ? <LiveHostView /> : <LiveParticipantView />;
};

export default LiveRoom;