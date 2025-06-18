<%--
  Created by IntelliJ IDEA.
  User: elenejobava
  Date: 6/17/25
  Time: 22:51
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<style>
  @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&display=swap');
  @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600&display=swap');

  body {
    margin: 0;
    padding: 0;
    font-family: Arial, sans-serif;
  }

  .top-line1 {
    position: relative;
    width: 100%;
    height: 190px;
    background-image: url('images/image.jpg');
    background-size: cover;
    background-position: center 25%;
    background-repeat: no-repeat;
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: hidden;
  }

  .top-line1::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.18);
    z-index: 1;
  }

  .top-line1 h2 {
    position: relative;
    z-index: 2;
    margin: 0;
    font-family: 'Poppins', sans-serif;
    color: #faf6e6;
    font-size: 24px;
    font-weight: 500;
    text-align: center;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
  }

  .top-line2 {
    position: relative;
    width: 100%;
    height: 50px;
    background-color: #d8eae6;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    box-sizing: border-box;
  }

  .library-title {
    display: flex;
    align-items: center;
  }

  .pale-text {
    color: #d0dddb;
    margin-right: 2px;
    font-size: 18px;
    font-weight: bold;
  }

  .highlight-text {
    color: rgba(141, 165, 161, 0.69);
    font-weight: bold;
    font-size: 18px;
  }

  .nav-container {
    display: flex;
    gap: 15px;
    align-items: center;
  }

  .nav-box {
    padding: 8px 16px;
    background-color: rgb(229, 236, 234);
    border: 1px solid rgb(221, 232, 230);
    border-radius: 6px;
    color: #b4c4c6;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s ease;
    text-decoration: none;
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .nav-box:hover {
    background-color: rgb(208, 221, 219);
    border-color: #e2e6e5;
    color: #4a5568;
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(202, 175, 96, 0.2);
  }

  .nav-box.active {
    background-color: #a7cdcd;
    color: #ffffff;
    border-color: #accac5;
  }

  .nav-icon {
    width: 16px;
    height: 16px;
    fill: currentColor;
  }

  .main-content {
    padding: 20px;
    min-height: 400px;
    background-color: #ffffff;
  }

  .sample-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 40px 20px;
  }

  .sample-content h3 {
    color: #333;
    font-family: 'Poppins', sans-serif;
    margin-bottom: 20px;
  }

  .sample-content p {
    color: #666;
    line-height: 1.6;
    margin-bottom: 15px;
  }

  .owl-icon {
    width: 30px;
    height: 30px;
    vertical-align: middle;
    margin-left: 10px;
  }

</style>
