DROP INDEX IF EXISTS customerIndex CASCADE;
DROP INDEX IF EXISTS mechanicIndex CASCADE;
DROP INDEX IF EXISTS carIndex CASCADE;
DROP INDEX IF EXISTS ownsIndex CASCADE;
DROP INDEX IF EXISTS serviceIndex CASCADE;
DROP INDEX IF EXISTS closedIndex CASCADE;

CREATE INDEX customerIndex
ON Customer
USING BTREE
(id);

CREATE INDEX mechanicIndex
ON Mechanic
USING BTREE
(id);

CREATE INDEX carIndex
ON Car
USING BTREE
(year);

CREATE INDEX ownsIndex
ON Owns
USING BTREE
(ownership_id);

CREATE INDEX serviceIndex
ON Service_Request
USING BTREE
(rid);

CREATE INDEX closedIndex
ON Closed_Request
USING BTREE
(date);

