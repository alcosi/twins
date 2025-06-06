-- new featurer FillerFieldUserFromApiUser
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2338, 23, 'org.twins.core.featurer.factory.filler.FillerFieldUserFromApiUser', 'Basics assignee from api user', '', false) on conflict on constraint featurer_pk do nothing ;
