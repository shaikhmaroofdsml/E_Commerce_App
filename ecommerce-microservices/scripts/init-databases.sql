-- ============================================================================
-- Enterprise E-Commerce Microservices Platform
-- MySQL Database Initialization Script
-- Run this as MySQL root: SOURCE scripts/init-databases.sql
-- ============================================================================

-- Create all service databases
CREATE DATABASE IF NOT EXISTS ecommerce_customers CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ecommerce_products  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ecommerce_orders    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ecommerce_payments  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ecommerce_notifications CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ecommerce_admin     CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create dedicated application user (allow from any host for Docker container networking)
CREATE USER IF NOT EXISTS 'ecommerce'@'%' IDENTIFIED BY 'ecommerce123';

-- Grant full access to all ecommerce databases
GRANT ALL PRIVILEGES ON ecommerce_customers.*    TO 'ecommerce'@'%';
GRANT ALL PRIVILEGES ON ecommerce_products.*     TO 'ecommerce'@'%';
GRANT ALL PRIVILEGES ON ecommerce_orders.*       TO 'ecommerce'@'%';
GRANT ALL PRIVILEGES ON ecommerce_payments.*     TO 'ecommerce'@'%';
GRANT ALL PRIVILEGES ON ecommerce_notifications.* TO 'ecommerce'@'%';
GRANT ALL PRIVILEGES ON ecommerce_admin.*        TO 'ecommerce'@'%';

FLUSH PRIVILEGES;

-- Verification
SELECT User, Host FROM mysql.user WHERE User = 'ecommerce';

SHOW DATABASES LIKE 'ecommerce_%';

SELECT 'Database initialization complete!' AS Status;
