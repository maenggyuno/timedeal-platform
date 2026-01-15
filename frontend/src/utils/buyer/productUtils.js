export const generateSampleProducts = (count) => {
    const products = [];
    for (let i = 1; i <= count; i++) {
      products.push({
        id: i,
        name: `상품 ${i}`,
        price: Math.floor(Math.random() * 50 + 10) * 1000,
        imageUrl: '/no-image.png',
      });
    }
    return products;
  };