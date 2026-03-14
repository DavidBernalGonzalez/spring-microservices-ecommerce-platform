DROP DATABASE IF EXISTS ms_products;
CREATE DATABASE IF NOT EXISTS ms_products;

USE ms_products;
SHOW TABLES;

SELECT * FROM categories;

INSERT INTO products (category_id, deleted, description, name, price, sku, status) VALUES
(1,0,'Laptop profesional con pantalla OLED y gran rendimiento','Laptop Studio Pro',2199.99,'SKU-LAPTOP-005','ACTIVE'),
(1,0,'Portátil ultraligero para viajes y trabajo remoto','Travel Laptop 13',1099.99,'SKU-LAPTOP-006','ACTIVE'),
(1,0,'Laptop para programación con 32GB RAM','Developer Laptop X',1999.99,'SKU-LAPTOP-007','ACTIVE'),
(1,0,'Laptop económica para tareas básicas','Laptop Essential',699.99,'SKU-LAPTOP-008','ACTIVE'),
(1,0,'Laptop convertible 2 en 1 con pantalla táctil','Laptop Flex 2in1',1299.99,'SKU-LAPTOP-009','ACTIVE'),
(1,0,'Laptop workstation para edición de vídeo','Workstation Laptop Z',2499.99,'SKU-LAPTOP-010','ACTIVE'),

(2,0,'Smartphone con batería de larga duración','Smartphone PowerMax',799.99,'SKU-PHONE-005','ACTIVE'),
(2,0,'Smartphone con triple cámara y 5G','Smartphone Vision',899.99,'SKU-PHONE-006','ACTIVE'),
(2,0,'Smartphone compacto de gama media','Smartphone Compact Plus',549.99,'SKU-PHONE-007','ACTIVE'),
(2,0,'Smartphone resistente al agua y polvo','Smartphone Outdoor',649.99,'SKU-PHONE-008','ACTIVE'),
(2,0,'Smartphone premium con pantalla AMOLED','Smartphone Elite',1199.99,'SKU-PHONE-009','ACTIVE'),
(2,0,'Smartphone económico para uso diario','Smartphone Basic',299.99,'SKU-PHONE-010','ACTIVE'),

(3,0,'Auriculares inalámbricos deportivos','Sport Wireless Earbuds',99.99,'SKU-AUDIO-005','ACTIVE'),
(3,0,'Auriculares estudio para audio profesional','Studio Headphones Pro',349.99,'SKU-AUDIO-006','ACTIVE'),
(3,0,'Altavoz inteligente con asistente de voz','Smart Speaker Home',149.99,'SKU-AUDIO-007','ACTIVE'),
(3,0,'Altavoz portátil compacto','Mini Bluetooth Speaker',59.99,'SKU-AUDIO-008','ACTIVE'),
(3,0,'Auriculares over-ear con cancelación activa','Noise Cancel Headphones',279.99,'SKU-AUDIO-009','ACTIVE'),
(3,0,'Barra de sonido para cine en casa','Soundbar Cinema',399.99,'SKU-AUDIO-010','ACTIVE'),

(4,0,'Monitor FullHD 22 pulgadas para oficina','Monitor Office 22"',159.99,'SKU-MONITOR-005','ACTIVE'),
(4,0,'Monitor 32 pulgadas 4K profesional','Monitor 4K 32"',799.99,'SKU-MONITOR-006','ACTIVE'),
(4,0,'Monitor gaming 240Hz','Gaming Monitor 240Hz',499.99,'SKU-MONITOR-007','ACTIVE'),
(4,0,'Monitor portátil USB-C para movilidad','Portable Monitor 15"',299.99,'SKU-MONITOR-008','ACTIVE'),
(4,0,'Monitor curvo 34 pulgadas ultrawide','Curved Ultrawide 34"',749.99,'SKU-MONITOR-009','ACTIVE'),
(4,0,'Monitor profesional para fotografía','Photo Editing Monitor',899.99,'SKU-MONITOR-010','ACTIVE'),

(5,0,'Teclado mecánico compacto 60%','Mechanical Keyboard 60%',129.99,'SKU-KEYBOARD-003','ACTIVE'),
(5,0,'Teclado silencioso para oficina','Office Silent Keyboard',49.99,'SKU-KEYBOARD-004','ACTIVE'),
(5,0,'Teclado gaming retroiluminado','Gaming Keyboard RGB Pro',179.99,'SKU-KEYBOARD-005','ACTIVE'),
(5,0,'Teclado inalámbrico slim','Wireless Slim Keyboard',59.99,'SKU-KEYBOARD-006','ACTIVE'),
(5,0,'Ratón vertical ergonómico','Vertical Ergonomic Mouse',79.99,'SKU-MOUSE-003','ACTIVE'),
(5,0,'Ratón gaming con DPI ajustable','Gaming Mouse X',119.99,'SKU-MOUSE-004','ACTIVE'),
(5,0,'Ratón inalámbrico compacto','Compact Wireless Mouse',39.99,'SKU-MOUSE-005','ACTIVE'),
(5,0,'Ratón profesional para diseño','Designer Precision Mouse',129.99,'SKU-MOUSE-006','ACTIVE'),
(5,0,'Alfombrilla gaming XL','Gaming Mouse Pad XL',29.99,'SKU-MOUSEPAD-001','ACTIVE'),
(5,0,'Soporte ergonómico para portátil','Laptop Stand Pro',69.99,'SKU-ACCESSORY-001','ACTIVE'),
(5,0,'Hub USB-C multipuerto','USB-C Hub 8in1',89.99,'SKU-ACCESSORY-002','ACTIVE'),
(5,0,'Base de refrigeración para laptop','Laptop Cooling Pad',49.99,'SKU-ACCESSORY-003','ACTIVE'),
(5,0,'Docking station USB-C','USB-C Dock Station',199.99,'SKU-ACCESSORY-004','ACTIVE'),
(5,0,'Webcam FullHD para videollamadas','Webcam FullHD Pro',89.99,'SKU-ACCESSORY-005','ACTIVE'),
(5,0,'Luz LED para streaming','Streaming LED Light',79.99,'SKU-ACCESSORY-006','ACTIVE');

SELECT * FROM products;