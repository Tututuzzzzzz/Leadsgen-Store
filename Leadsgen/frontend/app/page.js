"use client";

import { useEffect, useMemo, useState } from "react";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL || "https://dummyjson.com";
const USER_ID = Number(process.env.NEXT_PUBLIC_USER_ID || 1);
const EMPTY_CART = { cartId: null, userId: USER_ID, total: 0, discountedTotal: 0, itemCount: 0, items: [] };

export default function HomePage() {
  const [products, setProducts] = useState([]);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [cart, setCart] = useState(EMPTY_CART);
  const [quantities, setQuantities] = useState({});

  const [loadingProducts, setLoadingProducts] = useState(true);
  const [loadingCart, setLoadingCart] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    loadProducts();
    loadCartByUser();
  }, []);

  async function loadProducts() {
    setLoadingProducts(true);
    setError("");
    try {
      const response = await fetch(`${API_BASE}/products?limit=12`, { cache: "no-store" });
      if (!response.ok) {
        throw new Error("Cannot load products");
      }
      const data = await response.json();
      const list = Array.isArray(data?.products) ? data.products : [];
      setProducts(list);
      if (list.length > 0) {
        setSelectedProduct(list[0]);
      }
      const defaults = {};
      for (const product of list) {
        defaults[product.id] = 1;
      }
      setQuantities(defaults);
    } catch (err) {
      setError(err.message || "Unexpected error while loading products");
    } finally {
      setLoadingProducts(false);
    }
  }

  async function loadProductDetail(id) {
    setError("");
    try {
      const response = await fetch(`${API_BASE}/products/${id}`, { cache: "no-store" });
      if (!response.ok) {
        throw new Error("Cannot load product detail");
      }
      const data = await response.json();
      setSelectedProduct(data);
    } catch (err) {
      setError(err.message || "Unexpected error while loading product detail");
    }
  }

  async function loadCartByUser() {
    setLoadingCart(true);
    try {
      const response = await fetch(`${API_BASE}/carts/user/${USER_ID}`, { cache: "no-store" });
      if (response.status === 404) {
        setCart(EMPTY_CART);
        return;
      }
      if (!response.ok) {
        throw new Error("Cannot load cart");
      }
      const data = await response.json();
      const firstCart = Array.isArray(data?.carts) && data.carts.length > 0 ? data.carts[0] : null;
      setCart(firstCart ? normalizeCart(firstCart) : EMPTY_CART);
    } catch (err) {
      setError(err.message || "Unexpected error while loading cart");
    } finally {
      setLoadingCart(false);
    }
  }

  async function addToCart(productId) {
    setActionLoading(true);
    setError("");
    try {
      const requestQty = Math.max(1, Number(quantities[productId] || 1));
      const stock = getProductStock(productId);
      const inCartQty = Number(cartItemsByProductId.get(productId)?.quantity || 0);

      if (stock !== null) {
        const remaining = Math.max(0, stock - inCartQty);
        if (remaining <= 0) {
          throw new Error("This product reached max stock in cart");
        }
        if (requestQty > remaining) {
          setError(`Only ${remaining} item(s) can be added for this product`);
        }
      }

      const safeAddQty = stock === null
        ? requestQty
        : Math.min(requestQty, Math.max(0, stock - inCartQty));

      if (safeAddQty <= 0) {
        return;
      }

      const nextProducts = buildCartProductsPayload((current) => {
        const existing = current.find((item) => item.id === productId);
        if (existing) {
          return current.map((item) =>
            item.id === productId ? { ...item, quantity: item.quantity + safeAddQty } : item
          );
        }
        return [...current, { id: productId, quantity: safeAddQty }];
      });

      await syncCartWithDummy(nextProducts);
    } catch (err) {
      setError(err.message || "Unexpected error while adding item");
    } finally {
      setActionLoading(false);
    }
  }

  async function removeItem(productId) {
    setActionLoading(true);
    setError("");
    try {
      const nextProducts = buildCartProductsPayload((current) =>
        current.filter((item) => item.id !== productId)
      );

      if (nextProducts.length === 0) {
        setCart(EMPTY_CART);
        return;
      }

      await syncCartWithDummy(nextProducts);
    } catch (err) {
      setError(err.message || "Unexpected error while removing item");
    } finally {
      setActionLoading(false);
    }
  }

  async function updateQuantity(productId, quantity) {
    setActionLoading(true);
    setError("");
    try {
      const requestedQuantity = Math.max(1, Number(quantity || 1));
      const stock = getProductStock(productId);
      const safeQuantity = stock === null ? requestedQuantity : Math.min(requestedQuantity, stock);

      if (stock !== null && requestedQuantity > stock) {
        setError(`Max quantity for this product is ${stock}`);
      }

      const nextProducts = buildCartProductsPayload((current) =>
        current.map((item) =>
          item.id === productId ? { ...item, quantity: safeQuantity } : item
        )
      );

      await syncCartWithDummy(nextProducts);
    } catch (err) {
      setError(err.message || "Unexpected error while updating quantity");
    } finally {
      setActionLoading(false);
    }
  }

  function buildCartProductsPayload(transformer) {
    const current = (cart.items || []).map((item) => ({
      id: item.productId,
      quantity: Math.max(1, Number(item.quantity || 1))
    }));
    return transformer(current);
  }

  function getProductStock(productId) {
    const product = products.find((item) => item.id === productId);
    return product ? Number(product.stock || 0) : null;
  }

  async function syncCartWithDummy(productsPayload) {
    const response = await fetch(`${API_BASE}/carts/add`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        userId: USER_ID,
        products: productsPayload
      })
    });

    if (!response.ok) {
      const details = await safeJson(response);
      throw new Error(details?.message || "Cannot sync cart");
    }

    const data = await response.json();
    setCart(normalizeCart(data));
  }

  const cartItemsByProductId = useMemo(() => {
    const map = new Map();
    for (const item of cart.items || []) {
      map.set(item.productId, item);
    }
    return map;
  }, [cart]);

  const totalInventory = useMemo(
    () => products.reduce((sum, product) => sum + Number(product.stock || 0), 0),
    [products]
  );

  return (
    <main className="page">
      <div className="grain" />
      <header className="hero">
        <div className="hero-copy">
          <p className="eyebrow">Frontend Demo</p>
          <h1>Leadsgen Store</h1>
          <p className="subtitle">A modern Next.js storefront connected to DummyJSON public API.</p>
        </div>

        <div className="hero-stats">
          <article className="stat-card">
            <span>Products</span>
            <strong>{products.length}</strong>
          </article>
          <article className="stat-card">
            <span>Inventory Units</span>
            <strong>{totalInventory}</strong>
          </article>
          <article className="stat-card stat-emphasis">
            <span>Cart Total</span>
            <strong>{formatCurrency(cart.total || 0)}</strong>
          </article>
        </div>
      </header>

      <p className="api-path">API Endpoint: {API_BASE}</p>

      {error ? <p className="error">{error}</p> : null}

      <section className="layout">
        <div className="panel products">
          <div className="panel-title-row">
            <h2>Products</h2>
            {loadingProducts ? <span className="badge">Loading...</span> : <span className="badge">{products.length} items</span>}
          </div>

          <div className="product-grid">
            {products.map((product) => {
              const inCart = cartItemsByProductId.get(product.id);
              const currentInCart = Number(inCart?.quantity || 0);
              const maxAdd = Math.max(0, Number(product.stock || 0) - currentInCart);
              return (
                <article className="product-card" key={product.id}>
                  <button className="thumb-wrap" onClick={() => loadProductDetail(product.id)}>
                    <img src={product.thumbnail} alt={product.title} className="thumb" />
                  </button>
                  <h3>{product.title}</h3>
                  <p className="desc">{product.description}</p>
                  <div className="price-row">
                    <p className="price">{formatCurrency(product.price)}</p>
                    <span className="stock-pill">Stock {product.stock}</span>
                  </div>

                  <div className="cart-actions">
                    <input
                      type="number"
                      min={1}
                      max={Math.max(1, maxAdd)}
                      value={quantities[product.id] || 1}
                      onChange={(e) => {
                        const next = Number(e.target.value || 1);
                        const safeNext = Math.max(1, Math.min(next, Math.max(1, maxAdd)));
                        setQuantities((prev) => ({ ...prev, [product.id]: safeNext }));
                      }}
                    />
                    <button
                      className="btn-primary"
                      disabled={actionLoading || maxAdd <= 0}
                      onClick={() => addToCart(product.id)}
                    >
                      Add
                    </button>
                  </div>

                  {inCart ? <p className="incart">In cart: {inCart.quantity}</p> : <p className="incart muted">Not in cart</p>}
                </article>
              );
            })}
          </div>
        </div>

        <aside className="panel side">
          <section className="detail">
            <div className="panel-title-row">
              <h2>Product Detail</h2>
            </div>
            {!selectedProduct ? (
              <p className="muted">Choose a product to see details.</p>
            ) : (
              <div className="detail-card">
                <img src={selectedProduct.thumbnail} alt={selectedProduct.title} className="detail-img" />
                <h3>{selectedProduct.title}</h3>
                <p>{selectedProduct.description}</p>
                <div className="price-row">
                  <p className="price">{formatCurrency(selectedProduct.price)}</p>
                  <span className="stock-pill">Stock {selectedProduct.stock}</span>
                </div>
              </div>
            )}
          </section>

          <section className="cart">
            <div className="panel-title-row">
              <h2>Cart (User {USER_ID})</h2>
              {loadingCart ? <span className="badge">Loading...</span> : null}
            </div>

            {!cart.items || cart.items.length === 0 ? (
              <p className="muted">Cart is empty. Add some products.</p>
            ) : (
              <div className="cart-list">
                {cart.items.map((item) => (
                  <div className="cart-item" key={item.productId}>
                    <img src={item.thumbnail} alt={item.name} />
                    <div className="cart-meta">
                      <strong>{item.name}</strong>
                      <p>{formatCurrency(item.unitPrice)} each</p>
                      <p>Line: {formatCurrency(item.lineTotal)}</p>
                    </div>
                    <div className="cart-controls">
                      <input
                        className="cart-qty"
                        type="number"
                        min={1}
                        max={Math.max(1, getProductStock(item.productId) || 1)}
                        value={item.quantity}
                        onChange={(e) => updateQuantity(item.productId, Number(e.target.value || 1))}
                      />
                      <button className="danger cart-remove" onClick={() => removeItem(item.productId)}>
                        Remove
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}

            <footer className="cart-footer">
              <span>Total Items: {cart.itemCount || 0}</span>
              <span>Total: {formatCurrency(cart.total || 0)}</span>
            </footer>
          </section>
        </aside>
      </section>
    </main>
  );
}

function formatCurrency(value) {
  return Number(value || 0).toLocaleString("en-US", {
    style: "currency",
    currency: "USD"
  });
}

function normalizeCart(rawCart) {
  const rawItems = Array.isArray(rawCart?.products) ? rawCart.products : [];
  const items = rawItems
    .filter((item) => Number(item.quantity || 0) > 0)
    .map((item) => {
      const quantity = Number(item.quantity || 0);
      const unitPrice = Number(item.price || 0);
      const lineTotal = Number(item.total ?? unitPrice * quantity);
      return {
        productId: item.id,
        name: item.title,
        thumbnail: item.thumbnail,
        quantity,
        unitPrice,
        lineTotal
      };
    });

  return {
    cartId: rawCart?.id ?? null,
    userId: rawCart?.userId ?? USER_ID,
    total: Number(rawCart?.total ?? items.reduce((sum, item) => sum + item.lineTotal, 0)),
    discountedTotal: Number(rawCart?.discountedTotal ?? 0),
    itemCount: items.reduce((sum, item) => sum + item.quantity, 0),
    items
  };
}

async function safeJson(response) {
  try {
    return await response.json();
  } catch {
    return null;
  }
}
