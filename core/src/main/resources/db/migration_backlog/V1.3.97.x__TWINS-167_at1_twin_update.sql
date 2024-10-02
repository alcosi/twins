-- update for current user name into twin.name table
UPDATE twin
SET name = u.name
    FROM "user" u
WHERE twin.id = u.id
  AND u.name IS NOT NULL;
