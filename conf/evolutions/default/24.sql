# --- !Ups
CREATE TABLE contribution_history_contribution_feedback
(
  contribution_history_contribution_history_id bigint NOT NULL,
  contribution_feedback_id bigint NOT NULL,
  CONSTRAINT pk_contribution_history_feedback PRIMARY KEY (contribution_history_contribution_history_id, contribution_feedback_id),
  CONSTRAINT fk_contribution_history_feedback_01 FOREIGN KEY (contribution_history_contribution_history_id)
      REFERENCES contribution_history (contribution_history_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_contribution_history_feedback_02 FOREIGN KEY (contribution_feedback_id)
      REFERENCES contribution_feedback (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
# --- Downs