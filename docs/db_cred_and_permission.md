# 📄 PostgreSQL User, Roles & Permissions Setup for Spring Boot Application

## 🧩 Overview

This document explains how to:

* Create a database user
* Assign roles and permissions
* Configure access for a Spring Boot application
* Safely delete a database user

---

# 🛠️ 1. Create Database

```sql
CREATE DATABASE auth_service_db_local;
```

---

# 👤 2. Create User (Role)

```sql
CREATE ROLE auth_service_admin WITH LOGIN PASSWORD 'auth_service_admin';
```

👉 In PostgreSQL, a **user = role with LOGIN privilege**

---

# 🔐 3. Grant Database Access

```sql
GRANT CONNECT ON DATABASE auth_service_db_local TO auth_service_admin;
```

---

# 🧱 4. Grant Schema Permissions

```sql
GRANT USAGE ON SCHEMA public TO auth_service_admin;
GRANT CREATE ON SCHEMA public TO auth_service_admin;
```

---

# 📊 5. Grant Table Permissions

```sql
GRANT SELECT, INSERT, UPDATE, DELETE
ON ALL TABLES IN SCHEMA public
TO auth_service_admin;
```

---

# 🔢 6. Grant Sequence Permissions

```sql
GRANT USAGE, SELECT
ON ALL SEQUENCES IN SCHEMA public
TO auth_service_admin;
```

---

# 🔄 7. Default Privileges (Future Tables)

```sql
ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO auth_service_admin;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT USAGE, SELECT ON SEQUENCES TO auth_service_admin;
```

---

# ⚙️ 8. Spring Boot Configuration

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/auth_service_db_local
    username: auth_service_admin
    password: auth_service_admin

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

---

# 🧠 Permission Summary

| Permission | Purpose                   |
| ---------- | ------------------------- |
| CONNECT    | Access database           |
| USAGE      | Access schema             |
| CREATE     | Create tables (Hibernate) |
| SELECT     | Read data                 |
| INSERT     | Save data                 |
| UPDATE     | Modify data               |
| DELETE     | Remove data               |
| SEQUENCE   | Auto ID generation        |

---

# ⚠️ Development vs Production

## 🧪 Development

```yaml
ddl-auto: update
```

✔ Auto schema updates allowed

---

## 🚀 Production

```yaml
ddl-auto: validate
```

👉 Then remove CREATE permission:

```sql
REVOKE CREATE ON SCHEMA public FROM auth_service_admin;
```

---

# 🛡️ Best Practices

❌ Avoid:

```sql
GRANT ALL PRIVILEGES
```

✅ Follow:

* Principle of least privilege
* Separate users for app and migration

---

# ❌ 9. Delete Database User (Safe Way)

## ⚠️ Important

Before deleting a user:

* Remove active connections
* Handle owned objects

---

## 🔹 Step 1: Terminate Active Connections

```sql
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE usename = 'auth_service_admin';
```

---

## 🔹 Step 2: Reassign Owned Objects

```sql
REASSIGN OWNED BY auth_service_admin TO admin;
```

---

## 🔹 Step 3: Drop Owned Objects (Optional)

```sql
DROP OWNED BY auth_service_admin;
```

👉 ⚠️ This deletes all objects owned by the user

---

## 🔹 Step 4: Drop the User

```sql
DROP ROLE auth_service_admin;
```

---

## 🔥 Quick Cleanup (Dev Only)

```sql
REASSIGN OWNED BY auth_service_admin TO admin;
DROP OWNED BY auth_service_admin;
DROP ROLE auth_service_admin;
```

---

# ⚠️ Common Errors

### ❌ Error:

```
role "auth_service_admin" cannot be dropped because some objects depend on it
```

### ✅ Solution:

* Run:

```sql
REASSIGN OWNED BY auth_service_admin TO postgres;
```

---

# ✅ Final Checklist

✔ Database created
✔ User created
✔ Permissions assigned
✔ Spring Boot connected
✔ Safe deletion process documented

---

# 🎯 Conclusion

This setup ensures:

* Secure DB access
* Proper permission control
* Safe user lifecycle management
* Production-ready configuration

---
