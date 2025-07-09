<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<style>
    .book-details-container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 40px 20px;
        background: #ffffff;
    }

    .book-details {
        display: flex;
        gap: 30px;
        margin-bottom: 40px;
        flex-wrap: nowrap;
        align-items: flex-start;
    }

    .book-cover-large {
        flex: 0 0 250px;
        max-width: 250px;
        height: 350px;
        background: #f8f9fa;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .book-cover-large img {
        max-width: 100%;
        max-height: 100%;
        object-fit: cover;
    }

    .book-info-details {
        flex: 1;
        min-width: 0;
    }

    .book-title-large {
        font-family: 'Poppins', sans-serif;
        font-size: 2rem;
        font-weight: 600;
        color: #2d3748;
        margin: 0 0 10px 0;
    }

    .book-author-large {
        color: #718096;
        font-size: 1.2rem;
        font-weight: 400;
        margin: 0 0 15px 0;
    }

    .book-meta-details {
        display: flex;
        flex-wrap: wrap;
        gap: 15px;
        margin-bottom: 15px;
        color: #718096;
        font-size: 0.9rem;
    }

    .book-meta-item {
        display: flex;
        align-items: center;
        gap: 5px;
        width: 100%;
    }

    .book-meta-item svg {
        width: 16px;
        height: 16px;
        fill: #718096;
    }

    .book-description {
        color: #4a5568;
        line-height: 1.6;
        margin-bottom: 20px;
        font-size: 1rem;
    }

    .book-rating-large {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 20px;
    }

    .stars-large {
        color: #ffd700;
        font-size: 1.2rem;
        font-weight: bold;
    }

    .rating-text-large {
        color: #718096;
        font-size: 1rem;
    }

    .availability-status {
        font-size: 1rem;
        font-weight: 500;
        margin-bottom: 15px;
    }

    .availability-status.available {
        color: #38a169;
    }

    .availability-status.unavailable {
        color: #e53e3e;
    }

    .action-buttons {
        display: flex;
        gap: 15px;
        margin-bottom: 20px;
    }

    .reserve-button, .review-button {
        padding: 10px 20px;
        border-radius: 6px;
        font-size: 1rem;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .reserve-button {
        background-color: #a7cdcd;
        color: white;
        border: none;
    }

    .reserve-button:hover {
        background-color: #8bb8b8;
        transform: translateY(-1px);
    }

    .reserve-button:disabled {
        background-color: #e2e8f0;
        cursor: not-allowed;
    }

    .review-button {
        background-color: #f7f5f2;
        color: #8b7355;
        border: 1px solid #e0d6c7;
    }

    .review-button:hover {
        background-color: #f2ede6;
        color: #6b5a42;
        border-color: #d4c4b0;
        transform: translateY(-1px);
    }

    /* Admin button styles */
    .delete-button {
        background-color: #e53e3e;
        color: white;
        border: none;
        padding: 10px 20px;
        border-radius: 6px;
        font-size: 1rem;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .delete-button:hover {
        background-color: #c53030;
        transform: translateY(-1px);
    }

    .delete-button:disabled {
        background-color: #e2e8f0;
        cursor: not-allowed;
    }

    .back-to-admin-button {
        background-color: #4a5568;
        color: white;
        border: none;
        padding: 10px 20px;
        border-radius: 6px;
        font-size: 1rem;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .back-to-admin-button:hover {
        background-color: #2d3748;
        transform: translateY(-1px);
    }

    .review-form {
        display: none;
        margin-top: 20px;
        padding: 20px;
        background: #f8fafa;
        border-radius: 8px;
    }

    .review-form.show {
        display: block;
    }

    .review-form textarea {
        width: 100%;
        min-height: 100px;
        padding: 10px;
        border: 1px solid #e2e8f0;
        border-radius: 6px;
        font-size: 1rem;
        margin-bottom: 10px;
        resize: vertical;
    }



    .submit-review {
        padding: 10px 20px;
        background-color: #a7cdcd;
        color: white;
        border: none;
        border-radius: 6px;
        cursor: pointer;
        transition: all 0.3s ease;
    }

    .submit-review:hover {
        background-color: #8bb8b8;
        transform: translateY(-1px);
    }

    /* Enhanced sections styles */
    .book-header-section {
        margin-bottom: 20px;
        padding-bottom: 15px;
        border-bottom: 1px solid #e2e8f0;
    }

    .section-title {
        font-family: 'Poppins', sans-serif;
        font-size: 1.1rem;
        font-weight: 600;
        color: #2d3748;
        margin: 0 0 12px 0;
        padding-bottom: 6px;
        border-bottom: 2px solid #a7cdcd;
    }

    .book-metadata-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 20px;
        margin-bottom: 20px;
    }

    .metadata-section, .availability-section {
        padding: 15px;
        background: #f8fafa;
        border-radius: 8px;
        border: 1px solid #e2e8f0;
    }

    .availability-details p {
        margin: 6px 0;
        color: #4a5568;
        font-size: 0.9rem;
    }

    .actions-section {
        margin-bottom: 20px;
        padding: 15px;
        background: #f8fafa;
        border-radius: 8px;
        border: 1px solid #e2e8f0;
    }

    .description-section {
        margin-bottom: 20px;
    }

    /* Reviews Section Styles */
    .reviews-section {
        max-width: 1200px;
        margin: 40px auto 0;
        padding: 20px;
        background: #ffffff;
        border-top: 1px solid #e2e8f0;
    }

    .reviews-loading {
        text-align: center;
        color: #6c757d;
        padding: 20px;
    }

    .reviews-container {
        display: flex;
        flex-direction: column;
        gap: 20px;
    }

    .review-item {
        padding: 20px;
        background: #f8fafa;
        border-radius: 8px;
        border: 1px solid #e2e8f0;
    }

    .review-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10px;
        flex-wrap: wrap;
        gap: 10px;
    }

    .review-author {
        font-weight: 600;
        color: #2d3748;
        font-size: 1rem;
    }

    .review-rating {
        color: #ffd700;
        font-size: 18px;
        font-weight: bold;
    }

    .review-date {
        color: #718096;
        font-size: 0.9rem;
    }

    .review-text {
        color: #4a5568;
        line-height: 1.6;
        font-size: 0.95rem;
    }

    .no-reviews {
        text-align: center;
        color: #718096;
        padding: 40px 20px;
        font-style: italic;
    }

    .loading, .error {
        text-align: center;
        padding: 50px;
        font-size: 18px;
    }

    .error {
        color: #dc3545;
    }

    .loading {
        color: #6c757d;
    }

    /* Mobile responsiveness */
    @media (max-width: 768px) {
        .book-details {
            flex-direction: column;
            align-items: center;
            gap: 20px;
        }

        .book-cover-large {
            flex: 0 0 200px;
            max-width: 200px;
            height: 280px;
        }

        .book-metadata-grid {
            grid-template-columns: 1fr;
            gap: 15px;
        }

        .metadata-section, .availability-section, .actions-section {
            padding: 12px;
        }

        .section-title {
            font-size: 1rem;
        }

        .review-header {
            flex-direction: column;
            align-items: flex-start;
        }

        .book-title-large {
            font-size: 1.8rem;
        }

        .book-author-large {
            font-size: 1.1rem;
        }

        .action-buttons {
            flex-direction: column;
            gap: 10px;
        }

        .reserve-button, .review-button {
            width: 100%;
            justify-content: center;
        }
    }

    @media (max-width: 480px) {
        .book-cover-large {
            flex: 0 0 180px;
            max-width: 180px;
            height: 250px;
        }

        .book-title-large {
            font-size: 1.5rem;
        }

        .book-author-large {
            font-size: 1rem;
        }

        .book-details-container {
            padding: 20px 15px;
        }
    }

    .reserve-button.reserved {
        background-color: #6c757d !important;
        color: white !important;
        cursor: not-allowed !important;
    }

    .reserve-button.reserved:hover {
        background-color: #6c757d !important;
        transform: none !important;
    }

    .cancel-button {
        background-color: #dc3545 !important;
        color: white !important;
    }

    .cancel-button:hover {
        background-color: #c82333 !important;
        transform: translateY(-1px) !important;
    }

    /* Interactive Star Rating Styles */
    .star-rating-container {
        margin-bottom: 15px;
    }

    .rating-label {
        display: block;
        font-weight: 500;
        color: #2d3748;
        margin-bottom: 8px;
        font-size: 0.9rem;
    }

    .star-rating {
        display: flex;
        gap: 2px;
        margin-bottom: 5px;
    }

    .star {
        font-size: 1.8rem;
        color: #e2e8f0;
        cursor: pointer;
        transition: all 0.2s ease;
        user-select: none;
    }

    .star:hover {
        transform: scale(1.1);
        color: #ffd700;
    }

    .star.filled {
        color: #ffd700;
    }

    .star.hovered {
        color: #ffd700;
    }

    .rating-text {
        font-size: 0.85rem;
        color: #718096;
        font-style: italic;
    }

    .rating-text.selected {
        color: #2d3748;
        font-weight: 500;
    }

</style>