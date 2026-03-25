// Global State
let productsList = [];
let usersList = [];
let rolesList = [];
let permsList = [];
let currentUser = null;

document.addEventListener('DOMContentLoaded', () => {
    initApp();
});

function initApp() {
    document.getElementById('lang-selector').value = currentLang;

    // Check Auth State
    const userStr = localStorage.getItem('user');
    if (userStr && getTokens().accessToken) {
        currentUser = JSON.parse(userStr);
        showDashboard();
    } else {
        switchView('auth-view');
    }
}

// ================= ROUTING & VIEWS =================
function switchView(viewId) {
    document.querySelectorAll('.view').forEach(el => el.classList.remove('active'));
    document.getElementById(viewId).classList.add('active');
}

function showDashboard() {
    switchView('dashboard-view');
    
    // Set Sidebar info
    document.getElementById('nav-user-name').textContent = currentUser.name;
    document.getElementById('nav-avatar').textContent = currentUser.name.charAt(0).toUpperCase();
    
    const isAdmin = currentUser.roles.some(r => r === 'ROLE_ADMIN' || r === 'ADMIN');
    document.getElementById('nav-user-role').textContent = isAdmin ? 'Admin' : 'User';
    
    if (isAdmin) {
        document.getElementById('nav-users').style.display = 'flex';
    }

    // Load initial tab
    navigate('products', document.querySelector('.nav-item.active'));
}

function navigate(tab, element) {
    document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
    element.classList.add('active');

    document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
    document.getElementById(`tab-${tab}`).classList.add('active');

    if (tab === 'products') {
        document.getElementById('page-title').textContent = 'Products Management';
        fetchProducts();
    } else if (tab === 'users') {
        document.getElementById('page-title').textContent = 'User Roles & Access';
        fetchUsers();
        fetchMetadata();
    }
}

async function fetchMetadata() {
    try {
        if (rolesList.length === 0) rolesList = await apiFetch('/admin/users/roles');
        if (permsList.length === 0) permsList = await apiFetch('/admin/users/permissions');
    } catch (e) {}
}

function changeLanguage(lang) {
    currentLang = lang;
    localStorage.setItem('lang', lang);
    // Reload data with new language
    if (document.getElementById('tab-products').classList.contains('active')) fetchProducts();
    if (document.getElementById('tab-users').classList.contains('active')) fetchUsers();
}

// ================= AUTHENTICATION =================
function switchAuthTab(tab) {
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.getElementById(`tab-${tab}`).classList.add('active');

    document.getElementById('login-form').style.display = tab === 'login' ? 'block' : 'none';
    document.getElementById('register-form').style.display = tab === 'register' ? 'block' : 'none';
}

document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;
    const btn = document.getElementById('btn-login-submit');

    btn.disabled = true;
    try {
        const response = await apiFetch('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
        saveTokens(response.accessToken, response.refreshToken);
        localStorage.setItem('user', JSON.stringify(response.user));
        showToast('Welcome back!', 'success');
        setTimeout(() => location.reload(), 500);
    } catch (e) { } finally { btn.disabled = false; }
});

document.getElementById('register-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('reg-name').value;
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;
    const btn = document.getElementById('btn-reg-submit');

    btn.disabled = true;
    try {
        const response = await apiFetch('/auth/register', {
            method: 'POST',
            body: JSON.stringify({ name, email, password })
        });
        saveTokens(response.accessToken, response.refreshToken);
        localStorage.setItem('user', JSON.stringify(response.user));
        showToast('Registration successful!', 'success');
        setTimeout(() => location.reload(), 500);
    } catch (e) { } finally { btn.disabled = false; }
});

async function logout() {
    const { refreshToken } = getTokens();
    if (refreshToken) {
        try {
            await fetch(`${API_URL}/auth/logout`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ refreshToken })
            });
        } catch (e) {}
    }
    forceLogout();
}

// ================= PRODUCTS =================
async function fetchProducts() {
    try {
        productsList = await apiFetch('/products');
        renderProducts(productsList);
    } catch (error) {
        document.getElementById('products-table-body').innerHTML = `<tr><td colspan="6" class="text-center text-danger">Failed to load products</td></tr>`;
    }
}

function renderProducts(list) {
    const tbody = document.getElementById('products-table-body');
    tbody.innerHTML = '';
    
    if (!list || list.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center">No products found.</td></tr>`;
        return;
    }

    list.forEach(p => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>#${p.id}</td>
            <td><strong>${p.name}</strong><br><small class="text-secondary">${p.description || '--'}</small></td>
            <td>$${p.price.toFixed(2)}</td>
            <td>${p.stock}</td>
            <td class="text-secondary">${new Date(p.updatedAt || p.createdAt).toLocaleDateString()}</td>
            <td>
                <div class="action-btns">
                    <button class="btn-icon" onclick="editProduct(${p.id})" title="Edit">✏️</button>
                    <button class="btn-icon danger" onclick="deleteProduct(${p.id})" title="Delete">🗑️</button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function filterProducts(query) {
    query = query.toLowerCase();
    const filtered = productsList.filter(p => p.name.toLowerCase().includes(query) || (p.description && p.description.toLowerCase().includes(query)));
    renderProducts(filtered);
}

// Models
function openProductModal(prod = null) {
    document.getElementById('product-modal').classList.add('active');
    document.getElementById('overlay').classList.add('active');
    
    if (prod) {
        document.getElementById('product-modal-title').textContent = 'Edit Product';
        document.getElementById('prod-id').value = prod.id;
        document.getElementById('prod-name').value = prod.name;
        document.getElementById('prod-desc').value = prod.description || '';
        document.getElementById('prod-price').value = prod.price;
        document.getElementById('prod-stock').value = prod.stock;
    } else {
        document.getElementById('product-modal-title').textContent = 'Add Product';
        document.getElementById('product-form').reset();
        document.getElementById('prod-id').value = '';
    }
}

async function handleProductSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('prod-id').value;
    const payload = {
        name: document.getElementById('prod-name').value,
        description: document.getElementById('prod-desc').value,
        price: parseFloat(document.getElementById('prod-price').value),
        stock: parseInt(document.getElementById('prod-stock').value)
    };

    const btn = document.getElementById('btn-save-prod');
    btn.disabled = true;
    try {
        if (id) {
            await apiFetch(`/products/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
            showToast('Product updated!');
        } else {
            await apiFetch('/products', { method: 'POST', body: JSON.stringify(payload) });
            showToast('Product created!');
        }
        closeAllModals();
        fetchProducts();
    } catch (e) { } finally { btn.disabled = false; }
}

function editProduct(id) {
    const prod = productsList.find(p => p.id === id);
    if (prod) openProductModal(prod);
}

async function deleteProduct(id) {
    if (!confirm('Are you sure you want to delete this product?')) return;
    try {
        await apiFetch(`/products/${id}`, { method: 'DELETE' });
        showToast('Product deleted');
        fetchProducts();
    } catch (e) {}
}

// ================= USERS (ADMIN) =================
async function fetchUsers() {
    try {
        usersList = await apiFetch('/admin/users');
        renderUsers(usersList);
    } catch (error) {
        document.getElementById('users-table-body').innerHTML = `<tr><td colspan="6" class="text-center text-danger">Access Denied (Admin Only)</td></tr>`;
    }
}

function renderUsers(list) {
    const tbody = document.getElementById('users-table-body');
    tbody.innerHTML = '';
    
    list.forEach(u => {
        const rolesHtml = u.roles.map(r => `<span class="badge badge-user">${r.replace('ROLE_','')}</span>`).join(' ');
        const permsHtml = u.permissions.map(p => `<span class="badge badge-perm">${p}</span>`).join(' ');
        
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>#${u.id}</td>
            <td><strong>${u.name}</strong></td>
            <td>${u.email}</td>
            <td><span class="badge ${u.enabled?'badge-active':'badge-user'}">${u.enabled ? 'Active' : 'Disabled'}</span></td>
            <td>
                <div class="access-tags">
                    ${rolesHtml}
                    ${permsHtml}
                </div>
            </td>
            <td>
                <div class="action-btns">
                    <button class="btn-icon" onclick="openUserAccessModal(${u.id})" title="Manage Access">🛡️</button>
                    <button class="btn-icon danger" onclick="deleteUser(${u.id})" title="Delete">🗑️</button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// User Modal (Create)
async function openUserModal() {
    await fetchMetadata();
    
    document.getElementById('user-modal').classList.add('active');
    document.getElementById('overlay').classList.add('active');
    document.getElementById('user-form').reset();

    // Render roles
    const rolesDiv = document.getElementById('user-creation-roles');
    rolesDiv.innerHTML = rolesList.map(r => `
        <label class="checkbox-label">
            <input type="checkbox" name="roles" value="${r.replace('ROLE_', '')}"> ${r.replace('ROLE_', '')}
        </label>
    `).join('');

    // Render permissions
    const permsDiv = document.getElementById('user-creation-perms');
    permsDiv.innerHTML = permsList.map(p => `
        <label class="checkbox-label">
            <input type="checkbox" name="permissions" value="${p}"> ${p}
        </label>
    `).join('');
}

async function handleUserCreateSubmit(e) {
    e.preventDefault();
    const roles = Array.from(document.querySelectorAll('#user-creation-roles input:checked')).map(el => el.value);
    const permissions = Array.from(document.querySelectorAll('#user-creation-perms input:checked')).map(el => el.value);

    const payload = {
        name: document.getElementById('user-name').value,
        email: document.getElementById('user-email').value,
        password: document.getElementById('user-password').value,
        enabled: true,
        roles: roles,
        permissions: permissions
    };

    const btn = document.getElementById('btn-save-user');
    btn.disabled = true;
    try {
        await apiFetch('/admin/users', { method: 'POST', body: JSON.stringify(payload) });
        showToast('User created successfully!');
        closeAllModals();
        fetchUsers();
    } catch (e) {} finally { btn.disabled = false; }
}

// User Access Modal (Edit)
async function openUserAccessModal(id) {
    const user = usersList.find(u => u.id === id);
    if (!user) return;

    await fetchMetadata();
    
    document.getElementById('access-user-id').value = user.id;
    document.getElementById('access-user-name').textContent = user.name;
    
    // Render roles
    const rolesDiv = document.getElementById('access-roles-list');
    rolesDiv.innerHTML = rolesList.map(r => {
        const roleShort = r.replace('ROLE_', '');
        const checked = user.roles.includes(r) ? 'checked' : '';
        return `
            <label class="checkbox-label">
                <input type="checkbox" name="edit-roles" value="${roleShort}" ${checked}> ${roleShort}
            </label>
        `;
    }).join('');

    // Render perms
    const permsDiv = document.getElementById('access-perms-list');
    permsDiv.innerHTML = permsList.map(p => {
        const checked = user.permissions.includes(p) ? 'checked' : '';
        return `
            <label class="checkbox-label">
                <input type="checkbox" name="edit-perms" value="${p}" ${checked}> ${p}
            </label>
        `;
    }).join('');
    
    document.getElementById('user-access-modal').classList.add('active');
    document.getElementById('overlay').classList.add('active');
}

async function handleUserAccessSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('access-user-id').value;
    const roles = Array.from(document.querySelectorAll('#access-roles-list input:checked')).map(el => el.value);
    const permissions = Array.from(document.querySelectorAll('#access-perms-list input:checked')).map(el => el.value);
    
    const btn = document.getElementById('btn-save-access');
    btn.disabled = true;
    try {
        await apiFetch(`/admin/users/${id}/access`, {
            method: 'PUT',
            body: JSON.stringify({ roles, permissions })
        });
        showToast('Access updated!');
        closeAllModals();
        fetchUsers();
    } catch(e) {} finally { btn.disabled = false; }
}

async function deleteUser(id) {
    if (!confirm('Are you certain? This action cannot be undone.')) return;
    try {
        await apiFetch(`/admin/users/${id}`, { method: 'DELETE' });
        showToast('User deleted');
        fetchUsers();
    } catch(e) {}
}

function closeAllModals() {
    document.querySelectorAll('.modal').forEach(m => m.classList.remove('active'));
    document.getElementById('overlay').classList.remove('active');
}
