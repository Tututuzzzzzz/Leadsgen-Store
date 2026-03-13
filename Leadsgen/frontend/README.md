# Frontend Demo (Next.js 14)

## Run
Open 2 terminals:

1. Backend

```bash
cd Leadsgen/be
mvnw.cmd spring-boot:run
```

2. Frontend

```bash
cd Leadsgen/frontend
npm install
npm run dev
```

Then open:

```text
http://localhost:3000
```

## Environment
Create `.env.local` in `frontend` from `.env.local.example`:

```text
NEXT_PUBLIC_API_BASE_URL=https://dummyjson.com
NEXT_PUBLIC_USER_ID=1
```

## Features Included
- Product list (name, description, price, image, stock)
- Product detail by id (click product image)
- Add to cart with quantity
- View cart by userId=1
- Remove item from cart
- Update quantity in cart (bonus)
- Loading and error handling
- Responsive UI for desktop/mobile

## Suggested Screenshots For Submission
- Product list + detail visible on the same page
- Add to cart success with total updated
- Update quantity in cart (total changes)
- Remove item from cart
- Responsive view (mobile width)
