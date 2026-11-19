-- TWINS-943 face widget/twidget unification (phase 2): former WT widgets become pointed widgets.
-- Nullable target twin pointer; null means "target twin = current twin" (see FacePointedService.findPointedFace).
-- Only the released tables need ALTER; face_wt008 is still backlog-only, so it is created
-- with the column inline (see V1.4.394.07__TWINS-943_face_wt008_comments_widget.sql).

alter table face_wt001
    add column if not exists target_twin_pointer_id uuid
        references twin_pointer on update cascade on delete cascade;
create index if not exists face_wt001_target_twin_pointer_id_idx on face_wt001 (target_twin_pointer_id);

alter table face_wt002
    add column if not exists target_twin_pointer_id uuid
        references twin_pointer on update cascade on delete cascade;
create index if not exists face_wt002_target_twin_pointer_id_idx on face_wt002 (target_twin_pointer_id);

alter table face_wt003
    add column if not exists target_twin_pointer_id uuid
        references twin_pointer on update cascade on delete cascade;
create index if not exists face_wt003_target_twin_pointer_id_idx on face_wt003 (target_twin_pointer_id);
