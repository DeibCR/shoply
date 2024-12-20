let cartService;

class ShoppingCartService {

    cart = {
        items:[],
        total:0
    };

    addToCart(productId)
    {
        const url = `${config.baseUrl}/cart/products/${productId}`;
        const headers = userService.getHeaders();

        axios.post(url, {}, { headers })
            .then(response => {
                this.setCart(response.data)

                this.updateCartDisplay()
                this.showNotification("Product added to cart!");

            })
            .catch(error => {

                const data = {
                    error: "Add to cart failed."
                };

                templateBuilder.append("error", data, "errors")
            })
    }

    showNotification(message) {
        const container = document.getElementById("notification-container");
        if (!container) {
            console.error("Notification container not found.");
            return;
        }

        const notification = document.createElement("div");
        notification.classList.add("notification");
        notification.innerText = message;

        container.appendChild(notification);

        // Automatically remove the notification after 3 seconds
        setTimeout(() => {
            container.removeChild(notification);
        }, 3000);
    }

    setCart(data)
    {
        this.cart = {
            items: [],
            total: 0
        }

        this.cart.total = data.total;

        for (const [key, value] of Object.entries(data.items)) {
            this.cart.items.push(value);
        }
    }

    loadCart()
    {

        const url = `${config.baseUrl}/cart`;

        axios.get(url)
            .then(response => {
                this.setCart(response.data)

                this.updateCartDisplay()

            })
            .catch(error => {

                const data = {
                    error: "Load cart failed."
                };

                templateBuilder.append("error", data, "errors")
            })

    }

    loadCartPage() {

        console.log('Cart Data:', this.cart);


        const main = document.getElementById("main");
        if (!main) {
            console.error("Main container not found.");
            return;
        }


        main.innerHTML = "";
        let div = document.createElement("div");
        div.classList="filter-box";
        main.appendChild(div);


        const contentDiv = document.createElement("div");
        contentDiv.id = "content";
        contentDiv.classList.add("content-form");


        const cartHeader = document.createElement("div");
        cartHeader.classList.add("cart-header");

        const h1 = document.createElement("h1");
        h1.innerText = "Your cart";
        cartHeader.appendChild(h1);

        const button = document.createElement("button");
        button.classList.add("btn", "btn-danger");
        button.innerText = "Clear";
        button.addEventListener("click", () => this.clearCart());
        cartHeader.appendChild(button);

        contentDiv.appendChild(cartHeader);
        main.appendChild(contentDiv);

        const totalDiv = document.createElement("div");
        totalDiv.id = "cart-total";
        totalDiv.classList.add("cart-total");
        totalDiv.innerHTML = `<h3>Total: $${this.cart.total.toFixed(2)}</h3>`;
        contentDiv.appendChild(totalDiv);


        const itemListContainer = document.createElement("div");
        itemListContainer.id = "cart-item-list";

        if (this.cart.items.length > 0) {
            this.cart.items.forEach(item => {
                this.buildItem(item, itemListContainer);
            });
        } else {
            const emptyMessage = document.createElement("p");
            emptyMessage.innerText = "Your cart is empty.";
            itemListContainer.appendChild(emptyMessage);
        }

        contentDiv.appendChild(itemListContainer);
    }

    buildItem(item, parent) {
        let outerDiv = document.createElement("div");
        outerDiv.classList.add("cart-item");

        let div = document.createElement("div");
        outerDiv.appendChild(div);

        let h4 = document.createElement("h4");
        h4.innerText = item.product.name;
        div.appendChild(h4);

        let photoDiv = document.createElement("div");
        photoDiv.classList.add("photo");
        let img = document.createElement("img");
        img.src = `/images/products/${item.product.imageUrl}`;
        img.addEventListener("click", () => {
            showImageDetailForm(item.product.name, img.src);
        });
        photoDiv.appendChild(img);

        let priceH4 = document.createElement("h4");
        priceH4.classList.add("price");
        priceH4.innerText = `$${item.product.price}`;
        photoDiv.appendChild(priceH4);
        outerDiv.appendChild(photoDiv);

        let descriptionDiv = document.createElement("div");
        descriptionDiv.innerText = item.product.description;
        outerDiv.appendChild(descriptionDiv);

        let quantityDiv = document.createElement("div");
        quantityDiv.innerText = `Quantity: ${item.quantity}`;
        outerDiv.appendChild(quantityDiv);

        parent.appendChild(outerDiv);
    }


    clearCart()
    {

        const url = `${config.baseUrl}/cart`;

        axios.delete(url)
             .then(response => {
                 this.cart = {
                     items: [],
                     total: 0
                 }

                 this.cart.total = response.data.total;

                 for (const [key, value] of Object.entries(response.data.items)) {
                     this.cart.items.push(value);
                 }

                 this.updateCartDisplay()
                 this.loadCartPage()

             })
             .catch(error => {

                 const data = {
                     error: "Empty cart failed."
                 };

                 templateBuilder.append("error", data, "errors")
             })
    }

    updateCartDisplay()
    {
        try {
            const itemCount = this.cart.items.length;
            const cartControl = document.getElementById("cart-items")

            cartControl.innerText = itemCount;
        }
        catch (e) {

        }
    }
}





document.addEventListener('DOMContentLoaded', () => {
    cartService = new ShoppingCartService();

    if(userService.isLoggedIn())
    {
        cartService.loadCart();
    }

});
