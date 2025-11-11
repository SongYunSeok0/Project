
Refactor summary:
- Moved feature data to :data module (created new api, repository, di)
- Created domain repository interfaces and usecases
- App module now depends on :domain and :data
- Feature modules depend only on :domain
- Retrofit baseUrl set to https://example.com in data module NetworkModule
