# SmartShop Frontend

React + TypeScript + Vite frontend for SmartShop.

## Running locally

Requires Node.js 24 and npm.

From the repository root:

```bash
cd frontend
npm ci
npm run dev
```

Open the URL displayed by Vite.

The frontend requires the Spring Boot backend running on `http://localhost:8080`. To use another API address, set `VITE_API_BASE_URL` in `.env.local`.

## Testing

Run these commands from the `frontend` directory:

```bash
npm test
npm run test:regression
npm run lint
npm run build
```

The tests cover input validation, API requests, error handling and response ordering.

The regression check writes its output to `tests/evidence/`, which is excluded from Git.

## Manual verification

The following checks have not yet been completed against a running backend:

1. Create a store and categories, then assign categories to the store.
2. Add, edit and check shopping items.
3. Change the store's category order and verify the shopping list.
4. Test the Remaining view, New shopping trip and Delete purchased actions.
5. Restart the application and verify that stored data is preserved.

## Documentation

- [Specification](../docs/specification.md)
- [AI decisions](../docs/ai-decisions/ai-decisions.md)
