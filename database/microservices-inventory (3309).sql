CREATE DATABASE IF NOT EXISTS ms_inventory;

USE ms_inventory;
SHOW TABLES;

SELECT * FROM inventories;

SELECT * FROM inventory_movements;

DROP TABLE inventory_movements; 
DROP TABLE inventories;
