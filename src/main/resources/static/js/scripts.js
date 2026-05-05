document.addEventListener('DOMContentLoaded', function() {
    // Enable tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Enable popovers
    const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });

    // Auto-hide alerts after 5 seconds
    setTimeout(function() {
        const alerts = document.querySelectorAll('.alert');
        alerts.forEach(function(alert) {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        });
    }, 5000);

    // Check for notifications periodically
    if (document.getElementById('notificationCount')) {
        setInterval(fetchNotificationCount, 60000); // Check every minute
    }

    // Date validation for forms
    const startDateInput = document.getElementById('startsAt');
    const endDateInput = document.getElementById('endsAt');

    if (startDateInput && endDateInput) {
        startDateInput.addEventListener('change', function() {
            endDateInput.min = startDateInput.value;
        });

        endDateInput.addEventListener('change', function() {
            if (new Date(endDateInput.value) < new Date(startDateInput.value)) {
                alert('End time must be after start time');
                endDateInput.value = startDateInput.value;
            }
        });
    }

    // Rating stars interactive
    const ratingInputs = document.querySelectorAll('input[name="rating"]');
    if (ratingInputs.length > 0) {
        ratingInputs.forEach(function(input) {
            input.addEventListener('change', function() {
                const value = this.value;
                ratingInputs.forEach(function(inp, index) {
                    const label = inp.nextElementSibling;
                    if (index < value) {
                        label.innerHTML = '★';
                        label.style.color = '#ffc107';
                    } else {
                        label.innerHTML = '☆';
                        label.style.color = 'inherit';
                    }
                });
            });
        });
    }
});

// Function to fetch notification count
function fetchNotificationCount() {
    fetch('/api/notifications/count')
        .then(response => response.json())
        .then(count => {
            const notificationBadge = document.getElementById('notificationCount');
            if (count > 0) {
                notificationBadge.textContent = count;
                notificationBadge.style.display = 'inline-block';
            } else {
                notificationBadge.style.display = 'none';
            }
        })
        .catch(error => console.error('Error fetching notification count:', error));
}

// Form validation
function validateForm(formId) {
    const form = document.getElementById(formId);
    if (!form.checkValidity()) {
        event.preventDefault();
        event.stopPropagation();
    }
    form.classList.add('was-validated');
    return form.checkValidity();
}

// Format currency
function formatCurrency(amount) {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
}

// Date formatter
function formatDate(dateString) {
    const options = { year: 'numeric', month: 'long', day: 'numeric' };
    return new Date(dateString).toLocaleDateString('en-US', options);
}

// Time formatter
function formatTime(dateString) {
    const options = { hour: '2-digit', minute: '2-digit' };
    return new Date(dateString).toLocaleTimeString('en-US', options);
}

// DateTime formatter
function formatDateTime(dateString) {
    return formatDate(dateString) + ' ' + formatTime(dateString);
}