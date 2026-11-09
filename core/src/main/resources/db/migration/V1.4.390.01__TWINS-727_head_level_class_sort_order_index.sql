-- fix index
DROP INDEX IF EXISTS twin_head_twin_id_sort_order_index;
CREATE INDEX IF NOT EXISTS twin_head_twin_id_sort_order_index ON twin (head_twin_id, twin_class_id, sort_order);
