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

The frontend requires the Spring Boot backend running on `http://localhost:8080`; Vite proxies `/api` to it. The default frontend URL is `http://localhost:5173`. For another backend address, change the proxy target in `vite.config.ts`. `VITE_API_BASE_URL` is only suitable for a same-origin proxy or a server explicitly configured for cross-origin access; the backend does not enable CORS by default. In PowerShell with restricted execution policy, use `npm.cmd` in place of `npm`.

To run the entire application with Docker, see the [root README](../README.md).

## Testing

Run these commands from the `frontend` directory:

```bash
npm test
npm run test:regression
npm run lint
npm run build
```

The tests cover input validation, API requests, error handling and response ordering.

Quantity accepts both comma and point decimals in a text input with `inputMode="decimal"`. The comma is normalized before numeric parsing; the frontend sends the value without rounding. Spring Boot applies `HALF_UP` rounding to three decimal places and rejects values that become zero. The list and reopened editor use the saved value, displayed with a decimal comma and no unnecessary trailing zeros.

The regression check writes its output to `tests/evidence/`, which is excluded from Git.

## Manual verification

The user manually completed all five main browser scenarios and reported satisfactory results, with one exception: quantities could display more than three decimal places. The rounding correction has automated checks, and the user subsequently confirmed completion of its manual browser verification. See the [testing guide](../docs/testing.md#böngészős-ellenőrzések) for scenarios and recorded results, and the [milestone documentation](../docs/milestone-2.md) for the summary.

1. Create a store and categories, then assign categories to the store.
2. Add, edit and check shopping items.
3. Change the store's category order and verify the shopping list.
4. Test the Remaining view, New shopping trip and Delete purchased actions.
5. Restart the application and verify that stored data is preserved.

## Documentation

- [Specification](../docs/specification.md)
- [AI decisions index](../docs/ai-decisions/README.md)

The live test uses the production TypeScript API client against nginx, Spring Boot and PostgreSQL, without mocked fetch responses. Run it only against a disposable database: it creates data and performs the global reset/delete-purchased actions. It is intentionally excluded from `npm test`. See the [testing guide](../docs/testing.md#integráció-és-adatmegőrzés) for isolated Compose startup and persistence commands.
