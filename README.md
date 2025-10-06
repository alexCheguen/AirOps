# AirOps

Implementacion de calendario del módulo de vuelos.

## Autores
- Sharon Anelisse Marroquín Hernandez
- Eddy Alexander Cheguen Garcia

## Estructura del Proyecto

```
src/main/java/com/darwinruiz/airops
├── config/           
├── controllers/
├── converters/     
├── health/               
├── models/     
├── repositories/  
└── services/           
```

### Base de Datos
Se utilizo el 5433:5432

```bash
docker run --name postgres-airops -e POSTGRES_PASSWORD=admin123 -e POSTGRES_USER=postgres -e POSTGRES_DB=airops -p 5433:5432 -d postgres
```
