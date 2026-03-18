# Configurar Job en Jenkins (Build Now)

Guia paso a paso para crear el job del pipeline y ejecutar el primer build.

---

## Paso 1: Crear el Job

1. Abre Jenkins: **http://localhost:30080**
2. Clic en **New Item**
3. Nombre: `ecommerce-platform`
4. Tipo: **Pipeline**
5. Clic en **OK**

---

## Paso 2: Configurar el Pipeline

1. En la pagina de configuracion, baja a la seccion **Pipeline**
2. En **Definition** selecciona: **Pipeline script from SCM**
3. En **SCM** selecciona: **Git**
4. **Repository URL**: pega la URL de tu repositorio
   - GitHub: `https://github.com/TU-USUARIO/spring-microservices-ecommerce-platform.git`
   - GitLab: `https://gitlab.com/TU-USUARIO/spring-microservices-ecommerce-platform.git`
5. **Branch**: `*/main` (o `*/master` si usas master)
6. **Script Path**: `Jenkinsfile`
7. Clic en **Save**

---

## Paso 3: Credenciales (si el repo es privado)

Si tu repositorio es **privado**:

1. **Manage Jenkins** → **Credentials** → **Add Credentials**
2. Kind: **Username with password**
3. Username: tu usuario de GitHub/GitLab
4. Password: token de acceso personal (no la contraseña normal)
   - GitHub: Settings → Developer settings → Personal access tokens
   - GitLab: Preferences → Access Tokens
5. ID: `github` o `gitlab` (opcional)
6. Guardar

7. Volver al job → **Configure** → en **Credentials** selecciona las que creaste

---

## Paso 4: Ejecutar el Build

1. En la pagina del job `ecommerce-platform`
2. Clic en **Build Now**
3. Aparecera un nuevo build en **Build History**
4. Clic en el numero del build (ej. #1)
5. Clic en **Console Output** para ver el progreso

---

## Paso 5: Verificar

El pipeline deberia:

1. Clonar el repositorio
2. Crear un pod con Maven
3. Compilar y ejecutar tests de product-service
4. Compilar y ejecutar tests de inventory-service
5. Compilar y ejecutar tests de order-service
6. Compilar y ejecutar tests de gateway-service

Si todo va bien, el build aparecera en **azul** (Success).

---

## Problemas frecuentes

| Problema | Solucion |
|----------|----------|
| "No such file: Jenkinsfile" | Verifica que el Jenkinsfile esta en la raiz del repo |
| "Permission denied" (repo privado) | Añade credenciales en Jenkins |
| "Kubernetes pod failed" | Verifica que Kubernetes esta habilitado en Docker Desktop |
| Build se queda "pending" | Jenkins esta creando el pod; espera 1-2 minutos |
