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
        width: 150px;
        position: relative;
        margin-right: 30px;
    }

    .book-cover-large::before {
        content: "";
        position: absolute;
        top: -5px;
        left: -5px;
        right: -5px;
        bottom: -5px;
        background: rgba(255, 235, 150, 0.3);
        border-radius: 4px;
        z-index: -1;
        filter: blur(8px);
        opacity: 0;
        transition: opacity 0.3s ease;
    }

    .book-cover-large:hover img {
        transform: scale(1.02);
    }

    .book-cover-large:hover::before {
        opacity: 1;
    }

    .book-cover-large img {
        max-width: 100%;
        max-height: 100%;
        object-fit: cover;
        border-radius: 2px;
        transition: transform 0.3s ease;
    }

    .book-info-details {
        flex: 1;
        min-width: 0;
    }

    .book-title-large {
        font-family: 'Poppins', sans-serif;
        font-size: 2rem;
        font-weight: 500;
        color: #5d4e37;
        margin: 0 0 10px 0;
        letter-spacing: 0.5px;
    }

    .book-author-large {
        color: #8b7355;
        font-size: 1.2rem;
        font-weight: 400;
        margin: 0 0 15px 0;
    }

    .book-meta-details {
        display: flex;
        flex-wrap: wrap;
        gap: 15px;
        margin-bottom: 15px;
        color: #8b7355;
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
        fill: #8b7355;
    }

    .book-description {
        color: #7a6a3e;
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
        color: #e6cb58;
        font-size: 1.2rem;
        font-weight: bold;
    }

    .rating-text-large {
        color: #8b7355;
        font-size: 1rem;
    }

    .availability-status {
        font-size: 1rem;
        font-weight: 500;
        margin-bottom: 15px;
    }

    .availability-status.available {
        color: #2f855a;
    }

    .availability-status.unavailable {
        color: #d73a49;
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
        background: linear-gradient(135deg, #F5C45E 0%, #E8B850 100%);
        color: white;
        border: none;
        box-shadow: 0 4px 12px rgba(245, 196, 94, 0.3);
    }

    .reserve-button:hover {
        background: linear-gradient(135deg, #E8B850 0%, #DBAC48 100%);
        transform: translateY(-1px);
        box-shadow: 0 6px 16px rgba(219, 172, 72, 0.4);
    }

    .reserve-button:disabled {
        background: #e6cb58;
        cursor: not-allowed;
        transform: none;
        box-shadow: none;
        opacity: 0.5;
    }

    .review-button {
        background-color: #f9f7f4;
        color: #8b7355;
        border: 1px solid #e6cb58;
    }

    .review-button:hover {
        background-color: #f5f0e6;
        color: #5d4e37;
        border-color: #f5c45e;
        transform: translateY(-1px);
    }

    /* Admin button styles */
    .delete-button {
        background-color: #d73a49;
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
        background-color: #8b7355;
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
        background-color: #5d4e37;
        transform: translateY(-1px);
    }

    .review-form {
        display: none;
        margin-top: 20px;
        padding: 20px;
        background: #f9f7f4;
        border-radius: 8px;
        border: 1px solid #e6cb58;
    }

    .review-form.show {
        display: block;
    }

    .review-form textarea {
        width: 100%;
        min-height: 100px;
        padding: 10px;
        border: 1px solid #e6cb58;
        border-radius: 6px;
        font-size: 1rem;
        margin-bottom: 10px;
        resize: vertical;
        background: #fefdf8;
        color: #5d4e37;
    }

    .submit-review {
        padding: 10px 20px;
        background: linear-gradient(135deg, #F5C45E 0%, #E8B850 100%);
        color: white;
        border: none;
        border-radius: 6px;
        cursor: pointer;
        transition: all 0.3s ease;
        box-shadow: 0 4px 12px rgba(245, 196, 94, 0.3);
    }

    .submit-review:hover {
        background: linear-gradient(135deg, #E8B850 0%, #DBAC48 100%);
        transform: translateY(-1px);
        box-shadow: 0 6px 16px rgba(219, 172, 72, 0.4);
    }

    /* Enhanced sections styles */
    .book-header-section {
        margin-bottom: 20px;
        padding-bottom: 15px;
        border-bottom: 1px solid #e6cb58;
    }

    .section-title {
        font-family: 'Poppins', sans-serif;
        font-size: 1.1rem;
        font-weight: 600;
        color: #5d4e37;
        margin: 0 0 12px 0;
        padding-bottom: 6px;
        border-bottom: 2px solid #f5c45e;
    }

    .book-metadata-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 20px;
        margin-bottom: 20px;
    }

    .metadata-section, .availability-section {
        padding: 15px;
        background: #f9f7f4;
        border-radius: 8px;
        border: 1px solid #e6cb58;
    }

    .availability-details p {
        margin: 6px 0;
        color: #7a6a3e;
        font-size: 0.9rem;
    }

    .actions-section {
        margin-bottom: 20px;
        padding: 15px;
        background: #f9f7f4;
        border-radius: 8px;
        border: 1px solid #e6cb58;
    }

    .description-section {
        margin-bottom: 20px;
    }

    /* Reviews Section Styles */
    .reviews-section {
        max-width: 1200px;
        margin: 40px auto 0;
        padding: 20px;
        background: #faf8f0;
        border-top: 1px solid #e6cb58;
    }

    .reviews-loading {
        text-align: center;
        color: #8b7355;
        padding: 20px;
    }

    .reviews-container {
        display: flex;
        flex-direction: column;
        gap: 20px;
    }

    .review-item {
        padding: 20px;
        background: #f9f7f4;
        border-radius: 8px;
        border: 1px solid #e6cb58;
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
        color: #5d4e37;
        font-size: 1rem;
    }

    .review-rating {
        color: #e6cb58;
        font-size: 18px;
        font-weight: bold;
    }

    .review-date {
        color: #8b7355;
        font-size: 0.9rem;
    }

    .review-text {
        color: #7a6a3e;
        line-height: 1.6;
        font-size: 0.95rem;
    }

    .no-reviews {
        text-align: center;
        color: #8b7355;
        padding: 40px 20px;
        font-style: italic;
    }

    .loading, .error {
        text-align: center;
        padding: 50px;
        font-size: 18px;
    }

    .error {
        color: #d73a49;
    }

    .loading {
        color: #8b7355;
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
        background: #8b7355 !important;
        color: white !important;
        cursor: not-allowed !important;
    }

    .reserve-button.reserved:hover {
        background: #8b7355 !important;
        transform: none !important;
    }

    .cancel-button {
        background: linear-gradient(135deg, #d73a49 0%, #cb2431 100%) !important;
        color: white !important;
        box-shadow: 0 4px 12px rgba(203, 36, 49, 0.3) !important;
    }

    .cancel-button:hover {
        background: linear-gradient(135deg, #cb2431 0%, #b52d3a 100%) !important;
        transform: translateY(-1px) !important;
    }

    /* Interactive Star Rating Styles */
    .star-rating-container {
        margin-bottom: 15px;
    }

    .rating-label {
        display: block;
        font-weight: 500;
        color: #5d4e37;
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
        color: #f5f0e6;
        cursor: pointer;
        transition: all 0.2s ease;
        user-select: none;
    }

    .star:hover {
        transform: scale(1.1);
        color: #e6cb58;
    }

    .star.filled {
        color: #e6cb58;
    }

    .star.hovered {
        color: #e6cb58;
    }

    .rating-text {
        font-size: 0.85rem;
        color: #8b7355;
        font-style: italic;
    }

    .rating-text.selected {
        color: #5d4e37;
        font-weight: 500;
    }
</style>