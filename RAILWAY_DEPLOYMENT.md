# Railway Deployment Guide for Learning Management System

This guide will help you deploy your Learning Management System to Railway.

## Prerequisites

1. **Railway Account**: Sign up at [railway.app](https://railway.app)
2. **GitHub Repository**: Your code should be pushed to GitHub
3. **Railway CLI** (optional): Install from [railway.app/cli](https://railway.app/cli)

## Step 1: Prepare Your Repository

### Files Added for Railway Deployment

The following files have been added to your project:

- `Dockerfile` - Container configuration
- `railway.json` - Railway-specific configuration
- `.dockerignore` - Docker build optimization
- `src/main/resources/application-prod.yml` - Production configuration

### Environment Variables Setup

Your application now supports the following environment variables:

- `DATABASE_URL` - Database connection string (Railway provides this)
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `PORT` - Server port (Railway sets this automatically)
- `JWT_SECRET` - JWT secret key (generate a secure one)
- `JWT_EXPIRATION` - JWT expiration time in milliseconds
- `UPLOADCARE_PUBLIC_KEY` - Uploadcare public key
- `UPLOADCARE_SECRET_KEY` - Uploadcare secret key
- `UPLOADCARE_CDN_URL` - Uploadcare CDN URL

## Step 2: Deploy to Railway

### Method 1: Using Railway Dashboard (Recommended)

1. **Login to Railway**
   - Go to [railway.app](https://railway.app)
   - Sign in with your GitHub account

2. **Create New Project**
   - Click "New Project"
   - Select "Deploy from GitHub repo"
   - Choose your `LearningPlatform` repository
   - Select the `main` branch

3. **Add Database**
   - In your project dashboard, click "New"
   - Select "Database" → "PostgreSQL"
   - Railway will automatically provision a PostgreSQL database

4. **Configure Environment Variables**
   - Go to your service settings
   - Navigate to "Variables" tab
   - Add the following variables:

   ```
   SPRING_PROFILES_ACTIVE=prod
   JWT_SECRET=your-super-secure-jwt-secret-key-here-make-it-long-and-random
   UPLOADCARE_PUBLIC_KEY=your-uploadcare-public-key
   UPLOADCARE_SECRET_KEY=your-uploadcare-secret-key
   UPLOADCARE_CDN_URL=your-uploadcare-cdn-url
   ```

5. **Deploy**
   - Railway will automatically build and deploy your application
   - The build process will:
     - Build your Spring Boot application
     - Create a Docker container
     - Deploy to Railway's infrastructure

### Method 2: Using Railway CLI

1. **Install Railway CLI**
   ```bash
   npm install -g @railway/cli
   ```

2. **Login to Railway**
   ```bash
   railway login
   ```

3. **Initialize Project**
   ```bash
   railway init
   ```

4. **Add Database**
   ```bash
   railway add postgresql
   ```

5. **Set Environment Variables**
   ```bash
   railway variables set SPRING_PROFILES_ACTIVE=prod
   railway variables set JWT_SECRET=your-super-secure-jwt-secret-key
   railway variables set UPLOADCARE_PUBLIC_KEY=your-uploadcare-public-key
   railway variables set UPLOADCARE_SECRET_KEY=your-uploadcare-secret-key
   railway variables set UPLOADCARE_CDN_URL=your-uploadcare-cdn-url
   ```

6. **Deploy**
   ```bash
   railway up
   ```

## Step 3: Post-Deployment Configuration

### 1. Database Setup

Railway will automatically provide these environment variables:
- `DATABASE_URL` - Complete PostgreSQL connection string
- `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD` - Individual components

Your application will automatically:
- Connect to the PostgreSQL database
- Create tables using JPA/Hibernate
- Run any necessary migrations

### 2. File Storage

**Great News**: Your application is already configured to use **Uploadcare** for file storage! 

✅ **Uploadcare Integration** (Already implemented)
   - All files are uploaded directly to Uploadcare cloud storage
   - No local file storage needed
   - Files persist across deployments
   - CDN delivery for fast access
   - Automatic image optimization and transformations

**No additional setup needed** - your `UploadcareFileStorageService` handles everything!

### 3. Domain Configuration

1. **Custom Domain** (Optional)
   - Go to your service settings
   - Click "Settings" → "Domains"
   - Add your custom domain
   - Configure DNS records as instructed

2. **Railway Domain**
   - Railway provides a default domain like `your-app.railway.app`
   - This is immediately available after deployment

## Step 4: Testing Your Deployment

1. **Health Check**
   - Visit your Railway domain
   - You should see your application's home page

2. **Database Connection**
   - Try registering a new user
   - Check if data is persisted

3. **File Upload**
   - Test file uploads (they should work with Uploadcare)
   - Verify files are accessible

## Step 5: Monitoring and Maintenance

### Railway Dashboard Features

1. **Logs**
   - View real-time application logs
   - Debug any issues

2. **Metrics**
   - Monitor CPU, memory, and network usage
   - Set up alerts

3. **Deployments**
   - View deployment history
   - Rollback if needed

### Environment Management

1. **Production Environment**
   - Use `SPRING_PROFILES_ACTIVE=prod`
   - Optimized for performance

2. **Development Environment**
   - Use `SPRING_PROFILES_ACTIVE=dev`
   - More verbose logging

## Troubleshooting

### Common Issues

1. **Build Failures**
   - Check Dockerfile syntax
   - Verify all dependencies in pom.xml
   - Check build logs in Railway dashboard

2. **Database Connection Issues**
   - Verify `DATABASE_URL` is set correctly
   - Check if PostgreSQL service is running
   - Review database logs

3. **File Upload Issues**
   - Verify Uploadcare credentials are set correctly
   - Check Uploadcare account limits and quotas
   - Ensure Uploadcare public/secret keys are valid
   - Review Uploadcare CDN URL configuration

4. **Memory Issues**
   - Monitor memory usage in Railway dashboard
   - Consider upgrading to a higher plan
   - Optimize JVM settings

### Debugging Commands

```bash
# View logs
railway logs

# Connect to database
railway connect postgresql

# Check environment variables
railway variables

# Restart service
railway redeploy
```

## Security Considerations

1. **Environment Variables**
   - Never commit secrets to Git
   - Use Railway's environment variable system
   - Rotate JWT secrets regularly

2. **Database Security**
   - Railway provides secure database connections
   - Use SSL connections (enabled by default)

3. **Application Security**
   - Keep dependencies updated
   - Use HTTPS (Railway provides this)
   - Implement proper authentication

## Cost Optimization

1. **Railway Pricing**
   - Free tier: $5 credit monthly
   - Pay-as-you-go pricing
   - Monitor usage in dashboard

2. **Optimization Tips**
   - Use appropriate instance sizes
   - Optimize Docker image size
   - Implement proper caching

## Next Steps

1. **Set up CI/CD**
   - Configure automatic deployments
   - Set up staging environment

2. **Monitoring**
   - Set up application monitoring
   - Configure alerts

3. **Backup Strategy**
   - Regular database backups
   - File storage redundancy

4. **Performance Optimization**
   - Database indexing
   - Caching strategies
   - CDN setup

## Support

- **Railway Documentation**: [docs.railway.app](https://docs.railway.app)
- **Railway Community**: [discord.gg/railway](https://discord.gg/railway)
- **Your Application Logs**: Check Railway dashboard for detailed logs

---

**Note**: This deployment guide assumes you're using the default Railway PostgreSQL database. If you prefer MySQL, you can add a MySQL service instead, but you'll need to update the database configuration accordingly.
