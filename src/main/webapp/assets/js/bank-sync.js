// bank-sync.js - Xử lý việc đồng bộ và lưu transactions từ ngân hàng

const API_BASE = '/BudgetBuddy/api';

/**
 * Lưu transaction từ bank sync vào database
 * @param {Object} transactionData - Dữ liệu transaction từ form
 * @returns {Promise<Object>} - Kết quả lưu transaction
 */
async function saveBankTransaction(transactionData) {
    console.log('💾 Saving bank transaction:', transactionData);
    
    try {
        const response = await fetch(`${API_BASE}/bank-history/`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(transactionData)
        });
        
        console.log('📡 Response status:', response.status);
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error('❌ Server error:', errorText);
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        const result = await response.json();
        console.log('✅ Save result:', result);
        
        return result;
        
    } catch (error) {
        console.error('❌ Error saving transaction:', error);
        throw error;
    }
}

/**
 * Load danh sách categories để hiển thị trong dropdown
 * @returns {Promise<Array>} - Danh sách categories
 */
async function loadCategories() {
    try {
        const response = await fetch(`${API_BASE}/categories/`);
        
        if (!response.ok) {
            throw new Error(`Failed to load categories: ${response.status}`);
        }
        
        const result = await response.json();
        
        if (result.success && result.data) {
            return result.data;
        } else {
            throw new Error('Invalid response format');
        }
        
    } catch (error) {
        console.error('❌ Error loading categories:', error);
        return [];
    }
}

/**
 * Populate category dropdown
 * @param {string} selectElementId - ID của select element
 */
async function populateCategoryDropdown(selectElementId) {
    const selectElement = document.getElementById(selectElementId);
    if (!selectElement) {
        console.error('Select element not found:', selectElementId);
        return;
    }
    
    const categories = await loadCategories();
    
    // Clear existing options
    selectElement.innerHTML = '<option value="">Chọn danh mục</option>';
    
    // Add categories
    categories.forEach(function(cat) {
        // cat là Object[]: [id, name, type, icon_path]
        const option = document.createElement('option');
        option.value = cat[0]; // id
        option.textContent = `${cat[2] || '📂'} ${cat[1]}`; // icon + name
        option.dataset.type = cat[2]; // type
        selectElement.appendChild(option);
    });
    
    console.log('✅ Loaded', categories.length, 'categories');
}

/**
 * Handle save button click in modal
 * @param {string} modalId - ID của modal
 * @param {Object} transactionInfo - Thông tin transaction từ bank
 */
function handleSaveTransaction(modalId, transactionInfo) {
    const modal = document.getElementById(modalId);
    if (!modal) {
        console.error('Modal not found:', modalId);
        return;
    }
    
    // Get form data
    const categorySelect = modal.querySelector('select[name="category"]');
    const noteInput = modal.querySelector('input[name="note"]');
    
    if (!categorySelect || !noteInput) {
        alert('Không tìm thấy form elements!');
        return;
    }
    
    const categoryId = categorySelect.value;
    const note = noteInput.value.trim();
    
    // Validate
    if (!categoryId) {
        alert('Vui lòng chọn danh mục!');
        categorySelect.focus();
        return;
    }
    
    if (!note) {
        alert('Vui lòng nhập ghi chú!');
        noteInput.focus();
        return;
    }
    
    // Prepare data
    const transactionData = {
        categoryId: categoryId,
        note: note,
        amount: transactionInfo.amount,
        accountId: transactionInfo.accountId || null,
        bankReference: transactionInfo.reference || '',
        bankDescription: transactionInfo.description || ''
    };
    
    console.log('📝 Transaction data to save:', transactionData);
    
    // Show loading
    const saveButton = modal.querySelector('button.btn-save');
    if (saveButton) {
        saveButton.disabled = true;
        saveButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang lưu...';
    }
    
    // Save transaction
    saveBankTransaction(transactionData)
        .then(function(result) {
            if (result.success) {
                // Success
                alert('✅ Lưu giao dịch thành công!');
                
                // Close modal
                if (modal.classList.contains('modal')) {
                    modal.style.display = 'none';
                }
                
                // Reload page or update UI
                if (typeof loadData === 'function') {
                    loadData(); // Reload transactions list
                } else {
                    window.location.reload();
                }
            } else {
                throw new Error(result.message || 'Lưu thất bại');
            }
        })
        .catch(function(error) {
            alert('❌ Lỗi: ' + error.message);
            console.error('Save error:', error);
        })
        .finally(function() {
            // Reset button
            if (saveButton) {
                saveButton.disabled = false;
                saveButton.innerHTML = '<i class="fas fa-save"></i> Lưu';
            }
        });
}

/**
 * Initialize bank sync modal
 * @param {string} modalId - ID của modal
 * @param {Object} transactionInfo - Thông tin transaction
 */
function initBankSyncModal(modalId, transactionInfo) {
    console.log('🔧 Initializing bank sync modal:', modalId);
    
    const modal = document.getElementById(modalId);
    if (!modal) {
        console.error('Modal not found:', modalId);
        return;
    }
    
    // Populate category dropdown
    const categorySelect = modal.querySelector('select[name="category"]');
    if (categorySelect) {
        populateCategoryDropdown(categorySelect.id || 'categorySelect');
    }
    
    // Setup save button
    const saveButton = modal.querySelector('button.btn-save');
    if (saveButton) {
        saveButton.onclick = function() {
            handleSaveTransaction(modalId, transactionInfo);
        };
    }
    
    // Setup close button
    const closeButton = modal.querySelector('button.btn-close, .close');
    if (closeButton) {
        closeButton.onclick = function() {
            modal.style.display = 'none';
        };
    }
    
    console.log('✅ Modal initialized');
}

// Export functions for global use
window.saveBankTransaction = saveBankTransaction;
window.loadCategories = loadCategories;
window.populateCategoryDropdown = populateCategoryDropdown;
window.handleSaveTransaction = handleSaveTransaction;
window.initBankSyncModal = initBankSyncModal;
