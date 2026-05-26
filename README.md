### Ручки : 
```shell

Submissions
  POST   /sqlservice/submissions                    — отправить решение → 202 + submissionId
  GET    /sqlservice/submissions/{id}               — статус посылки (поллинг)
  GET    /sqlservice/submissions?taskId=&userId=    — список с фильтрами
  GET    /sqlservice/submissions/leaderboard?taskId= — рейтинг по задаче

Tasks
  POST   /sqlservice/tasks                          — создать задачу
  GET    /sqlservice/tasks                          — все задачи
  GET    /sqlservice/tasks?databaseId=1             — задачи конкретной БД
  GET    /sqlservice/tasks/{id}                     — одна задача
  PATCH  /sqlservice/tasks/{id}                     — частичное обновление
  DELETE /sqlservice/tasks/{id}                     — удалить (кеш тоже)

Databases
  POST   /sqlservice/databases                      — зарегистрировать БД
  GET    /sqlservice/databases                      — все БД
  GET    /sqlservice/databases/{id}                 — одна БД
  DELETE /sqlservice/databases/{id}                 — удалить

Containers
  POST   /sqlservice/containers                     — зарегистрировать контейнер
  GET    /sqlservice/containers                     — все контейнеры
  GET    /sqlservice/containers?status=RUNNING      — с фильтром по статусу
  PATCH  /sqlservice/containers/{id}/status?value=  — сменить статус + evict DataSource
  DELETE /sqlservice/containers/{id}               — удалить + evict DataSource
```