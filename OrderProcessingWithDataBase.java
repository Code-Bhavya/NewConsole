package withUpdate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderProcessingWithDataBase {
	static String url = "jdbc:mysql://localhost:3306/shopping";
	static String userName = "root";
	static String password = "Mysql@2024";

	public static Connection createDBConnection() throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.cj.jdbc.Driver");
		return DriverManager.getConnection(url, userName, password);
	}

	public static void createTable() throws ClassNotFoundException {
		String createProductTableSQL = "CREATE TABLE IF NOT EXISTS productdetails (" + "ProductCode INT PRIMARY KEY, "
				+ "ProductName VARCHAR(100) NOT NULL, " + "price DOUBLE NOT NULL, " + "quantity INT NOT NULL )";

//		String createOrdersTableSQL = "CREATE TABLE IF NOT EXISTS orders (" + "orderId INT PRIMARY KEY AUTO_INCREMENT, "
//				+ "productCode INT NOT NULL, " + "quantity INT NOT NULL)";

		try (Connection connection = createDBConnection(); Statement stmt = connection.createStatement()) {
			stmt.execute(createProductTableSQL);
//			stmt.execute(createOrdersTableSQL);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void insertProducts(Product product) throws ClassNotFoundException {
		String insertSQL = "INSERT INTO productdetails (ProductCode, ProductName, price, quantity) VALUES (?, ?, ?, ?)";
		try (Connection connection = createDBConnection();
				PreparedStatement ps = connection.prepareStatement(insertSQL)) {
			ps.setInt(1, product.getProductCode());
			ps.setString(2, product.getProductName());
			ps.setDouble(3, product.getPrice());
			ps.setInt(4, product.getQuantity());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static List<Product> readAllProducts() throws ClassNotFoundException {
		List<Product> products = new ArrayList<>();
		String selectSQL = "SELECT * FROM productdetails";

		try (Connection connection = createDBConnection();
				Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(selectSQL)) {
			while (rs.next()) {
				int productCode = rs.getInt("ProductCode");
				String productName = rs.getString("ProductName");
				double price = rs.getDouble("price");
				int quantity = rs.getInt("quantity");
				products.add(new Product(productCode, productName, price, quantity));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return products;
	}

	public static Product readProductByCode(int productCode) throws ClassNotFoundException {
		String selectSQL = "SELECT * FROM productdetails WHERE ProductCode = ?";
		Product product = null;
		try (Connection connection = createDBConnection();
				PreparedStatement ps = connection.prepareStatement(selectSQL)) {
			ps.setInt(1, productCode);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					String productName = rs.getString("ProductName");
					double price = rs.getDouble("price");
					int quantity = rs.getInt("quantity");
					product = new Product(productCode, productName, price, quantity);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return product;
	}

	public static boolean deleteProduct(int productCode) throws ClassNotFoundException {
		String deleteSQL = "DELETE FROM productdetails WHERE ProductCode = ?";
		try (Connection connection = createDBConnection();
				PreparedStatement ps = connection.prepareStatement(deleteSQL)) {
			ps.setInt(1, productCode);
			int rowsAffected = ps.executeUpdate();
			return rowsAffected > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void updateProduct(Product product) throws ClassNotFoundException {
		String updateSQL = "UPDATE productdetails SET ProductName = ?, price = ?, quantity = ? WHERE ProductCode = ?";
		try (Connection connection = createDBConnection();
				PreparedStatement ps = connection.prepareStatement(updateSQL)) {
			ps.setString(1, product.getProductName());
			ps.setDouble(2, product.getPrice());
			ps.setInt(3, product.getQuantity());
			ps.setInt(4, product.getProductCode());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static boolean placeOrder(int productCode, int quantity) throws ClassNotFoundException {
		String updateProductSQL = "UPDATE productdetails SET quantity = quantity - ? WHERE ProductCode = ? AND quantity >= ?";
		String insertOrderSQL = "INSERT INTO orders (productCode, quantity) VALUES (?, ?)";

		try (Connection connection = createDBConnection();
				PreparedStatement updateProductStmt = connection.prepareStatement(updateProductSQL);
				PreparedStatement insertOrderStmt = connection.prepareStatement(insertOrderSQL)) {

			connection.setAutoCommit(false);

			updateProductStmt.setInt(1, quantity);
			updateProductStmt.setInt(2, productCode);
			updateProductStmt.setInt(3, quantity);
			int rowsAffected = updateProductStmt.executeUpdate();

			if (rowsAffected > 0) {
				insertOrderStmt.setInt(1, productCode);
				insertOrderStmt.setInt(2, quantity);
				insertOrderStmt.executeUpdate();
				connection.commit();
				return true;
			} else {
				connection.rollback();
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void displayProductDetails() throws ClassNotFoundException {
		List<Product> products = readAllProducts();
		if (products != null && !products.isEmpty()) {
			for (Product product : products) {
				System.out.println(product);
			}
		} else {
			System.out.println("No products available.");
		}
	}
}
