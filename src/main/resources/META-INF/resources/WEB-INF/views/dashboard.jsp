<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Artist Tracker Dashboard</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome for icons -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <style>
        .artist-card {
            transition: transform 0.2s;
        }
        .artist-card:hover {
            transform: translateY(-2px);
        }
        .work-image {
            width: 150px;
            height: 200px;
            object-fit: cover;
            border-radius: 8px;
        }
        .work-card {
            margin-bottom: 20px;
        }
        .loading {
            display: none;
        }
        .artist-info {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        .toast {
            min-width: 300px;
        }

        /* Theme-specific styles */
        :root {
            --bg-color: #f8f9fa;
            --card-bg: #ffffff;
            --text-color: #212529;
            --border-color: #dee2e6;
            --input-bg: #ffffff;
            --input-border: #ced4da;
            --shadow: rgba(0, 0, 0, 0.1);
        }

        [data-theme="dark"] {
            --bg-color: #1a1a1a;
            --card-bg: #2d3748;
            --text-color: #ffffff;
            --border-color: #4a5568;
            --input-bg: #374151;
            --input-border: #4a5568;
            --shadow: rgba(0, 0, 0, 0.3);
        }

        body {
            background-color: var(--bg-color) !important;
            color: var(--text-color) !important;
            transition: background-color 0.3s, color 0.3s;
        }

        .card {
            background-color: var(--card-bg) !important;
            border-color: var(--border-color) !important;
            color: var(--text-color) !important;
        }

        .form-control {
            background-color: var(--input-bg) !important;
            border-color: var(--input-border) !important;
            color: var(--text-color) !important;
        }

        .form-control:focus {
            background-color: var(--input-bg) !important;
            border-color: #86b7fe !important;
            color: var(--text-color) !important;
            box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25) !important;
        }

        .form-control::placeholder {
            color: var(--text-color) !important;
            opacity: 0.6;
        }

        .form-label {
            color: var(--text-color) !important;
        }

        .modal-content {
            background-color: var(--card-bg) !important;
            color: var(--text-color) !important;
        }

        .dropdown-menu {
            background-color: var(--card-bg) !important;
            border-color: var(--border-color) !important;
        }

        .dropdown-item {
            color: var(--text-color) !important;
        }

        .dropdown-item:hover {
            background-color: var(--border-color) !important;
        }

        .theme-dropdown {
            min-width: 120px;
        }

        .theme-icon {
            width: 16px;
            height: 16px;
            margin-right: 8px;
        }
    </style>
</head>
<body class="bg-light">
<div class="container-fluid py-4">
    <!-- Header -->
    <div class="row mb-4">
        <div class="col-12">
            <div class="card shadow">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start mb-4">
                        <h1 class="card-title mb-0">
                            <i class="fas fa-palette"></i> Artist Tracker Dashboard
                        </h1>

                        <!-- Theme Dropdown -->
                        <div class="dropdown">
                            <button class="btn btn-outline-secondary dropdown-toggle theme-dropdown" type="button" id="themeDropdown" data-bs-toggle="dropdown" aria-expanded="false">
                                <i class="fas fa-sun theme-icon" id="themeIcon"></i>
                                <span id="themeText">Light</span>
                            </button>
                            <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="themeDropdown">
                                <li>
                                    <a class="dropdown-item" href="#" onclick="setTheme('light')">
                                        <i class="fas fa-sun theme-icon"></i>
                                        Light
                                    </a>
                                </li>
                                <li>
                                    <a class="dropdown-item" href="#" onclick="setTheme('dark')">
                                        <i class="fas fa-moon theme-icon"></i>
                                        Dark
                                    </a>
                                </li>
                                <li>
                                    <a class="dropdown-item" href="#" onclick="setTheme('auto')">
                                        <i class="fas fa-adjust theme-icon"></i>
                                        Auto
                                    </a>
                                </li>
                            </ul>
                        </div>
                    </div>

                    <!-- Add Artist Section -->
                    <div class="row align-items-end">
                        <div class="col-md-6">
                            <label for="artistName" class="form-label">Artist Name</label>
                            <input type="text" class="form-control" id="artistName" placeholder="Enter artist name">
                        </div>
                        <div class="col-md-3">
                            <button class="btn btn-primary w-100" onclick="addArtist()">
                                <i class="fas fa-plus"></i> Add Artist
                            </button>
                        </div>
                        <div class="col-md-3">
                            <button class="btn btn-success w-100" onclick="refreshAllArtists()">
                                <i class="fas fa-sync-alt"></i> Refresh All
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Loading Indicator -->
    <div class="row loading" id="loadingIndicator">
        <div class="col-12 text-center">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
            <p class="mt-2">Processing request...</p>
        </div>
    </div>

    <!-- Artists List -->
    <div class="row" id="artistsList">
        <!-- Artists will be loaded here via AJAX -->
    </div>

    <!-- New Works Modal -->
    <div class="modal fade" id="newWorksModal" tabindex="-1">
        <div class="modal-dialog modal-xl">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">New Works Found</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body" id="newWorksContent">
                    <!-- New works will be displayed here -->
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Toast Container -->
<div id="toastContainer" class="position-fixed top-0 end-0 p-3" style="z-index: 9999;">
    <!-- Toasts will be added here -->
</div>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<!-- jQuery -->
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>

<script>
    $(document).ready(function() {
        loadAllArtists();
        initializeTheme();
    });

    // Theme management functions
    function initializeTheme() {
        // Get saved theme from localStorage or default to 'light'
        const savedTheme = localStorage.getItem('theme') || 'light';
        setTheme(savedTheme);
    }

    function setTheme(theme) {
        localStorage.setItem('theme', theme);

        let actualTheme = theme;

        // Handle auto theme
        if (theme === 'auto') {
            actualTheme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
        }

        // Apply theme
        if (actualTheme === 'dark') {
            document.documentElement.setAttribute('data-theme', 'dark');
            document.body.classList.remove('bg-light');
            document.body.classList.add('bg-dark');
        } else {
            document.documentElement.removeAttribute('data-theme');
            document.body.classList.remove('bg-dark');
            document.body.classList.add('bg-light');
        }

        // Update dropdown button
        updateThemeButton(theme, actualTheme);

        // Removed toast notification for theme switching
    }

    function updateThemeButton(selectedTheme, actualTheme) {
        const themeIcon = document.getElementById('themeIcon');
        const themeText = document.getElementById('themeText');

        const themes = {
            'light': { icon: 'fas fa-sun', text: 'Light' },
            'dark': { icon: 'fas fa-moon', text: 'Dark' },
            'auto': { icon: 'fas fa-adjust', text: 'Auto' }
        };

        const theme = themes[selectedTheme];
        themeIcon.className = `${theme.icon} theme-icon`;
        themeText.textContent = theme.text;
    }

    // Listen for system theme changes when auto theme is selected
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
        const currentTheme = localStorage.getItem('theme');
        if (currentTheme === 'auto') {
            setTheme('auto');
        }
    });

    // Rest of your existing JavaScript functions remain the same...
    function showLoading() {
        $('#loadingIndicator').show();
    }

    function hideLoading() {
        $('#loadingIndicator').hide();
    }

    function showToast(message, type = 'success') {
        const toastId = 'toast-' + Date.now();
        const toastHtml =
            '<div id="' + toastId + '" class="toast align-items-center text-white bg-' + type + ' border-0" role="alert" aria-live="assertive" aria-atomic="true">' +
            '<div class="d-flex">' +
            '<div class="toast-body">' + message + '</div>' +
            '<button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>' +
            '</div>' +
            '</div>';
        $('#toastContainer').append(toastHtml);
        const toastElement = document.getElementById(toastId);
        const toast = new bootstrap.Toast(toastElement, {
            autohide: true,
            delay: 5000
        });
        toast.show();

        // Remove toast element after it's hidden
        toastElement.addEventListener('hidden.bs.toast', function () {
            toastElement.remove();
        });
    }

    function formatDate(dateString) {
        try {
            const date = new Date(dateString);
            return date.toLocaleString('en-US', {
                year: 'numeric',
                month: 'short',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit'
            });
        } catch (e) {
            return dateString;
        }
    }

    function loadAllArtists() {
        showLoading();
        $.ajax({
            url: '/api/artists/all',
            method: 'GET',
            success: function(artists) {
                displayArtists(artists);
                hideLoading();
            },
            error: function(xhr, status, error) {
                console.error('Failed to load artists:', error);
                showToast('Failed to load artists', 'danger');
                hideLoading();
            }
        });
    }

    function displayArtists(artists) {
        const artistsList = $('#artistsList');
        artistsList.empty();

        if (artists.length === 0) {
            artistsList.append(
                '<div class="col-12">' +
                '<div class="text-center py-5">' +
                '<i class="fas fa-user-plus fa-3x text-muted mb-3"></i>' +
                '<h4 class="text-muted">No artists tracked yet</h4>' +
                '<p class="text-muted">Add an artist to get started</p>' +
                '</div>' +
                '</div>'
            );
            return;
        }

        artists.forEach(artist => {
            const formattedDate = formatDate(artist.lastUpdated);
            const escapedArtistName = escapeHtml(decodeURIComponent(artist.artistName));
            const escapedServiceName = escapeHtml(artist.serviceName);
            const escapedLatestWorkId = escapeHtml(artist.latestId);

            const artistCard =
                '<div class="col-md-6 col-lg-4 mb-4">' +
                '<div class="card artist-card shadow-sm h-100">' +
                '<div class="card-header artist-info">' +
                '<h5 class="mb-1">' +
                '<i class="fas fa-user"></i> ' + escapedArtistName +
                '</h5>' +
                '<small class="opacity-75">Service: ' + escapedServiceName + '</small>' +
                '</div>' +
                '<div class="card-body">' +
                '<p class="card-text">' +
                '<strong>Latest ID:</strong> ' + escapedLatestWorkId + '<br>' +
                '<strong>Last Released:</strong> ' + formattedDate +
                '</p>' +
                '</div>' +
                '<div class="card-footer bg-transparent">' +
                '<div class="btn-group w-100">' +
                '<button class="btn btn-outline-primary" onclick="checkSingleArtist(\'' + escapedArtistName + '\')">' +
                '<i class="fas fa-sync"></i> Refresh' +
                '</button>' +
                '<button class="btn btn-outline-danger" onclick="deleteArtist(\'' + escapedArtistName + '\')">' +
                '<i class="fas fa-trash"></i> Delete' +
                '</button>' +
                '</div>' +
                '</div>' +
                '</div>' +
                '</div>';
            artistsList.append(artistCard);
        });
    }

    function escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function addArtist() {
        const artistName = $('#artistName').val().trim();
        if (!artistName) {
            showToast('Please enter an artist name', 'warning');
            return;
        }

        showLoading();
        $.ajax({
            url: '/api/artists/' + encodeURIComponent(artistName),
            method: 'POST',
            success: function() {
                showToast('Artist "' + artistName + '" added successfully');
                $('#artistName').val('');
                loadAllArtists();
            },
            error: function(xhr) {
                hideLoading();
                if (xhr.status === 400) {
                    showToast('Artist already exists or invalid name', 'warning');
                } else {
                    showToast('Failed to add artist', 'danger');
                }
            }
        });
    }

    function deleteArtist(artistName) {
        if (!confirm('Are you sure you want to delete "' + artistName + '"?')) {
            return;
        }

        showLoading();
        $.ajax({
            url: '/api/artists/' + encodeURIComponent(artistName),
            method: 'DELETE',
            success: function() {
                showToast('Artist "' + artistName + '" deleted successfully');
                loadAllArtists();
            },
            error: function() {
                hideLoading();
                showToast('Failed to delete artist', 'danger');
            }
        });
    }

    function checkSingleArtist(artistName) {
        showLoading();
        $.ajax({
            url: '/api/artists/' + encodeURIComponent(artistName) + '/check',
            method: 'GET',
            success: function(newWorks) {
                hideLoading();
                if (newWorks.length > 0) {
                    displayNewWorks(newWorks, 'New works for ' + artistName);
                } else {
                    showToast('No new works found for ' + artistName, 'info');
                }
            },
            error: function() {
                hideLoading();
                showToast('Failed to check ' + artistName, 'danger');
            }
        });
    }

    function refreshAllArtists() {
        showLoading();
        $.ajax({
            url: '/api/artists/refresh-all',
            method: 'POST',
            success: function(allNewWorks) {
                hideLoading();
                if (allNewWorks.length > 0) {
                    displayNewWorks(allNewWorks, 'New works found');
                    loadAllArtists(); // Refresh the artists list
                } else {
                    showToast('No new works found for any artist', 'info');
                }
            },
            error: function() {
                hideLoading();
                showToast('Failed to refresh all artists', 'danger');
            }
        });
    }

    function displayNewWorks(newWorks, title) {
        $('#newWorksModal .modal-title').text(title);
        const content = $('#newWorksContent');
        content.empty();

        const worksHtml = newWorks.map(work => {
            const formattedDate = formatDate(work.date);
            const proxiedImageUrl = '/api/image-proxy?url=' + encodeURIComponent(work.firstImageUrl);
            const escapedTitle = escapeHtml(work.title);
            const artistName = escapeHtml(work.artist);
            const escapedId = escapeHtml(work.id);
            const escapedGalleryUrl = escapeHtml(work.galleryUrl);

            return '<div class="col-md-6 col-lg-4 work-card">' +
                '<div class="card h-100">' +
                '<img src="' + proxiedImageUrl + '"' +
                ' class="card-img-top work-image mx-auto mt-3"' +
                ' alt="' + escapedTitle + '"' +
                ' onerror="this.src=\'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZGRkIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCwgc2Fucy1zZXJpZiIgZm9udC1zaXplPSIxNCIgZmlsbD0iIzk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPk5vIEltYWdlPC90ZXh0Pjwvc3ZnPg==\'">' +
                '<div class="card-body">' +
                '<h6 class="card-title">' + escapedTitle + '</h6>' +
                '<p class="card-text">' +
                '<small class="text-muted">' +
                'Artist: ' + artistName + '<br>' +
                '<small class="text-muted">' +
                'ID: ' + escapedId + '<br>' +
                'Date: ' + formattedDate +
                '</small>' +
                '</p>' +
                '</div>' +
                '<div class="card-footer">' +
                '<a href="' + escapedGalleryUrl + '" target="_blank" class="btn btn-primary btn-sm w-100">' +
                '<i class="fas fa-external-link-alt"></i> View Gallery' +
                '</a>' +
                '</div>' +
                '</div>' +
                '</div>';
        }).join('');

        content.html('<div class="row">' + worksHtml + '</div>');
        new bootstrap.Modal($('#newWorksModal')).show();
    }

    // Allow adding artist with Enter key
    $('#artistName').keypress(function(e) {
        if (e.which === 13) {
            addArtist();
        }
    });

    // Enhanced image error handling
    $(document).on('error', '.work-image', function() {
        console.log('Image failed to load via proxy:', this.src);
        // Fallback already handled by onerror attribute
    });
</script>
</body>
</html>
