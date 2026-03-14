DROP DATABASE IF EXISTS ms_products;
CREATE DATABASE IF NOT EXISTS ms_products;

USE ms_products;
SHOW TABLES;

SELECT * FROM categories;

INSERT INTO products (category_id, deleted, description, name, price, sku, status) VALUES
(1, 0, 'Laptop potente para desarrollo y trabajo profesional', 'Laptop Pro 16', 1899.99, 'SKU-LAPTOP-001', 'ACTIVE'),
(1, 0, 'Portátil ligero ideal para estudiantes y movilidad', 'Laptop Air 13', 1199.99, 'SKU-LAPTOP-002', 'ACTIVE'),
(2, 0, 'Smartphone de gama alta con cámara avanzada', 'Smartphone Ultra X', 999.99, 'SKU-PHONE-001', 'ACTIVE'),
(2, 0, 'Smartphone compacto con gran batería', 'Smartphone Mini', 699.99, 'SKU-PHONE-002', 'ACTIVE'),
(3, 0, 'Auriculares inalámbricos con cancelación de ruido', 'Wireless Headphones', 249.99, 'SKU-AUDIO-001', 'ACTIVE'),
(3, 0, 'Altavoz bluetooth portátil resistente al agua', 'Bluetooth Speaker', 129.99, 'SKU-AUDIO-002', 'ACTIVE'),
(4, 0, 'Monitor 27 pulgadas 4K ideal para diseño', 'Monitor 4K 27"', 449.99, 'SKU-MONITOR-001', 'ACTIVE'),
(4, 0, 'Monitor ultrawide 34 pulgadas para multitarea', 'Ultrawide Monitor 34"', 699.99, 'SKU-MONITOR-002', 'ACTIVE'),
(5, 0, 'Teclado mecánico RGB para programadores', 'Mechanical Keyboard RGB', 149.99, 'SKU-KEYBOARD-001', 'ACTIVE'),
(5, 0, 'Ratón ergonómico inalámbrico de alta precisión', 'Wireless Ergonomic Mouse', 89.99, 'SKU-MOUSE-001', 'ACTIVE');

SELECT * FROM products;