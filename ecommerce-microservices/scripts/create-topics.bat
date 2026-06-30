@echo off
REM ============================================================================
REM Kafka Topic Creation Script — Enterprise E-Commerce Platform
REM Run AFTER Kafka is started (KRaft mode).
REM Default Kafka path: C:\kafka — edit KAFKA_HOME below if different.
REM ============================================================================

SET KAFKA_HOME=C:\kafka
SET BOOTSTRAP=localhost:9092

echo Creating Kafka topics for E-Commerce platform...
echo.

REM ── Order Topics ──────────────────────────────────────────────────────────
%KAFKA_HOME%\bin\windows\kafka-topics.bat --create --if-not-exists ^
  --topic order.created ^
  --bootstrap-server %BOOTSTRAP% ^
  --partitions 3 ^
  --replication-factor 1
echo [OK] order.created

%KAFKA_HOME%\bin\windows\kafka-topics.bat --create --if-not-exists ^
  --topic order.created.DLT ^
  --bootstrap-server %BOOTSTRAP% ^
  --partitions 1 ^
  --replication-factor 1
echo [OK] order.created.DLT (Dead Letter Topic)

REM ── Payment Topics ─────────────────────────────────────────────────────────
%KAFKA_HOME%\bin\windows\kafka-topics.bat --create --if-not-exists ^
  --topic payment.completed ^
  --bootstrap-server %BOOTSTRAP% ^
  --partitions 3 ^
  --replication-factor 1
echo [OK] payment.completed

%KAFKA_HOME%\bin\windows\kafka-topics.bat --create --if-not-exists ^
  --topic payment.completed.DLT ^
  --bootstrap-server %BOOTSTRAP% ^
  --partitions 1 ^
  --replication-factor 1
echo [OK] payment.completed.DLT

%KAFKA_HOME%\bin\windows\kafka-topics.bat --create --if-not-exists ^
  --topic payment.failed ^
  --bootstrap-server %BOOTSTRAP% ^
  --partitions 3 ^
  --replication-factor 1
echo [OK] payment.failed

%KAFKA_HOME%\bin\windows\kafka-topics.bat --create --if-not-exists ^
  --topic payment.failed.DLT ^
  --bootstrap-server %BOOTSTRAP% ^
  --partitions 1 ^
  --replication-factor 1
echo [OK] payment.failed.DLT

REM ── Notification Topics ────────────────────────────────────────────────────
%KAFKA_HOME%\bin\windows\kafka-topics.bat --create --if-not-exists ^
  --topic notification.send ^
  --bootstrap-server %BOOTSTRAP% ^
  --partitions 1 ^
  --replication-factor 1
echo [OK] notification.send

REM ── Inventory Topics ──────────────────────────────────────────────────────
%KAFKA_HOME%\bin\windows\kafka-topics.bat --create --if-not-exists ^
  --topic inventory.update ^
  --bootstrap-server %BOOTSTRAP% ^
  --partitions 3 ^
  --replication-factor 1
echo [OK] inventory.update

echo.

REM List all created topics
echo ── All Kafka Topics ────────────────────────────────────────────────────
%KAFKA_HOME%\bin\windows\kafka-topics.bat --list --bootstrap-server %BOOTSTRAP%

echo.
echo Topic creation complete!
pause
